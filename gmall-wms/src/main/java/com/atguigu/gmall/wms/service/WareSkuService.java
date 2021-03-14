package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-22 16:36:38
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken);
}

