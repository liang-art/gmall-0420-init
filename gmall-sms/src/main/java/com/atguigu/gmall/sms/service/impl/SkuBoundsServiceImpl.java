package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionMapper reductionMapper;

    @Autowired
    private SkuLadderMapper ladderMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    //保存销售信息
    @Transactional
    @Override
    public void saveSales(SkuSaleVo skuSaleVo) {
        // 3.1.保存sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        List<Integer> works = skuSaleVo.getWork();
        if (!CollectionUtils.isEmpty(works) && works.size() == 4) {
            skuBoundsEntity.setWork(works.get(3) * 8 + works.get(2) * 4 + works.get(1) * 2 + works.get(0));
        }
        this.save(skuBoundsEntity);

        // 3.2.保存sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, reductionEntity);
        reductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        this.reductionMapper.insert(reductionEntity);

        // 3.3.保存sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        this.ladderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesItemBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos = new ArrayList<>();

        //查询积分优惠
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id",skuId));
        if(skuBoundsEntity!=null){
            ItemSaleVo skuBoundsSaleVo = new ItemSaleVo();
            skuBoundsSaleVo.setType("积分");
            skuBoundsSaleVo.setDesc("赠送"+skuBoundsEntity.getGrowBounds()+"成长积分,赠送"+skuBoundsEntity.getBuyBounds()+"购买积分");
            itemSaleVos.add(skuBoundsSaleVo);
        }

        //查询满减优惠
        SkuFullReductionEntity skuFullReductionEntity = this.reductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id",skuId));
        if(skuFullReductionEntity!=null){
            ItemSaleVo skuReductionSaleVo = new ItemSaleVo();
            skuReductionSaleVo.setType("满减");
            skuReductionSaleVo.setDesc("满"+skuFullReductionEntity.getFullPrice()+"减"+skuFullReductionEntity.getReducePrice());
            itemSaleVos.add(skuReductionSaleVo);
        }

        //查询打折优惠
        SkuLadderEntity skuLadderEntity = this.ladderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id",skuId));
        if(skuLadderEntity!=null){
            ItemSaleVo skuLadderSaleVo = new ItemSaleVo();
            skuLadderSaleVo.setType("打折");
            skuLadderSaleVo.setDesc("满多少"+skuLadderEntity.getFullCount()+"件，打"+skuLadderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            itemSaleVos.add(skuLadderSaleVo);
        }

        return itemSaleVos;
    }

}