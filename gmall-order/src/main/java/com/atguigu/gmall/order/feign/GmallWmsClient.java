package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName:GmallWmsClient
 * Package:com.atguigu.gmall.item.feign
 * Date:2020/10/12 23:22
 *
 * @Author:com.bjpowernode
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
    @PostMapping("wms/waresku/check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkAndLock(@RequestBody List<SkuLockVo> lockVos, @PathVariable("orderToken")String orderToken);
}
