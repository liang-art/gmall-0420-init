package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName:GmallWmsApi
 * Package:com.atguigu.gmall.wms.api
 * Date:2020/9/27 19:42
 *
 * @Author:com.bjpowernode
 */

public interface GmallWmsApi {
    //7.根据skuId查询库存信息
    //根据skuId查询所有库存
    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuEntityBySkuId(@PathVariable("skuId") Long skuId);

    @PostMapping("check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkAndLock(@RequestBody List<SkuLockVo> lockVos, @PathVariable("orderToken")String orderToken);
}
