package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-21 20:23:52
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {

    List<SkuAttrValueEntity> querySaleAttrsBySpuId(Long spuId);

    List<Map<String, Object>> querySkuAttrValuesMap(Long spuId);
}
