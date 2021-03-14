package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data//锁定库存的VO
public class SkuLockVo {

    private Long skuId;
    private Integer count;

    private Boolean lock; // 锁定状态
    private Long wareSkuId; // 锁定成功的库存id，以方便将来解锁该仓库库存
}
