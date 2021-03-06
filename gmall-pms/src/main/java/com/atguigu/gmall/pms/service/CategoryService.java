package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-21 20:23:52
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategoryList(Long pid);

    List<CategoryEntity> queryLel2Catagory(Long pid);

    List<CategoryEntity> queryAllCategoriesByCid3(Long cid3);
}

