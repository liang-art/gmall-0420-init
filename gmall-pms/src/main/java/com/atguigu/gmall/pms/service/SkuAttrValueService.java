package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-21 20:23:52
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySkuAttrValuebyCidAndSkuId(Long cid, Long skuId);

    List<SaleAttrValueVo> querySaleAttrsBySpuId(Long spuId);

    String querySkuAttrValues(Long spuId);
}

