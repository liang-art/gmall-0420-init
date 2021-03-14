package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class SpuAttrValueVo extends SpuAttrValueEntity {//继承字段，并且拓展字段

    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
