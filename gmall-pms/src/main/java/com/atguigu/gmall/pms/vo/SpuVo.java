package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;
import java.util.List;

@Data
public class SpuVo extends SpuEntity { //继承字段，并且拓展字段
    //根据json字符串，创建vo视图
    private List<String> spuImages;

    private List<SpuAttrValueVo> baseAttrs;

    private List<SkuVo> skus;
}
