package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import jdk.internal.util.xml.impl.Attrs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;
import sun.font.AttributeValues;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    private AttrService attrService;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValuebyCidAndSkuId(Long cid, Long skuId) {
        //根据分类id和销售类型查询规格参数表
        List<AttrEntity> attrEntities = attrService.list(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));
       //对集合判空
        if(CollectionUtils.isEmpty(attrEntities)){
            //如果为空则下面不执行
            return null;
        }
        //根据skuId和attrIds查询销售检索类型的规格参数和值
        List<Long> ids = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", ids));

        return skuAttrValueEntities;
    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrsBySpuId(Long spuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.querySaleAttrsBySpuId(spuId);
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
            //将skuAttrValueEntities数组转化位map集合
            Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
            //将map集合遍历
            map.forEach((attrId,attrValueEntities)->{
                SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
                saleAttrValueVo.setAttrId(attrId);
                saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
                saleAttrValueVo.setAttrValues(attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
                saleAttrValueVos.add(saleAttrValueVo);
            });
            return saleAttrValueVos;
        }
        return null;
    }

    @Override
    public String querySkuAttrValues(Long spuId) {
        List<Map<String, Object>> maps = this.skuAttrValueMapper.querySkuAttrValuesMap(spuId);

        if (CollectionUtils.isEmpty(maps)){
            return null;
        }
        Map<String, Long> jsonMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map -> (Long)map.get("sku_id")));
        System.out.println(jsonMap);
        return JSON.toJSONString(jsonMap);
    }

}