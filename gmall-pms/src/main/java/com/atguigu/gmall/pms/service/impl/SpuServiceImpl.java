package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper descMapper;

    @Autowired
    private SpuAttrValueService baseAttrService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SkuAttrValueService attrValueService;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuPage(Long cid, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果存在category_id，则使用此字段查询
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }
        String key = pageParamVo.getKey();
        //如果条件存在，则按照条件进行查询
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }


    //全局事务注解
    @GlobalTransactional
//    @Transactional//本地事务，当bigSave报错时，bigSave下的数据会回滚。
                    // 远程调用的saveSkuSalesVo会成功，不会回滚
    @Override
    public void bigSave(SpuVo spu) {
        // 1.保存spu相关信息
        // 1.1.保存pms_spu
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        Long spuId = spu.getId();

        // 1.2.保存pms_spu_desc
        if (!CollectionUtils.isEmpty(spu.getSpuImages())) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spu.getSpuImages(), ","));
            this.descMapper.insert(spuDescEntity);
        }

        // 1.3.保存pms_spu_attr_value
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
//            List<SpuAttrValueEntity> spuAttrValueEntities = new ArrayList<>();
//            baseAttrs.forEach(spuAttrValueVo -> {
//                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
//                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
//                spuAttrValueEntity.setSpuId(spuId);
//                spuAttrValueEntity.setSort(0);
//                spuAttrValueEntities.add(spuAttrValueEntity);
//            });

            this.baseAttrService.saveBatch(baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                spuAttrValueEntity.setSort(0);
                return spuAttrValueEntity;
            }).collect(Collectors.toList()));
        }

        // 2.保存sku相关信息
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return ;
        }
        skus.forEach(skuVo -> {
            // 2.1.保存pms_sku
            skuVo.setSpuId(spuId);
            skuVo.setCatagoryId(spu.getCategoryId());
            skuVo.setBrandId(spu.getBrandId());
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)){
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage(): images.get(0));
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            // 2.2.保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)){
                imagesService.saveBatch(images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSort(0);
                    if (StringUtils.equals(image, skuVo.getDefaultImage())){
                        skuImagesEntity.setDefaultStatus(1);
                    }
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }

            // 2.3.保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(attr -> {
                    attr.setSkuId(skuId);
                    attr.setSort(0);
                });
                this.attrValueService.saveBatch(saleAttrs);
            }

            // 3.保存sku营销信息
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.smsClient.saveSkuSalesVo(skuSaleVo);
//            int i = 10/0;
        });

        rabbitTemplate.convertAndSend("SEARCH-EXCHANGE","search.save",spuId);
    }

}

