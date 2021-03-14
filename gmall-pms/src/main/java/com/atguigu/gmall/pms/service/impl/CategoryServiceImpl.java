package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoryList(Long pid) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        //如果pid=-1则进行全表查询，否则按pid中进行查询
        if(pid != -1){
            wrapper.eq("parent_id",pid);
        }

        return this.list(wrapper);
    }

    @Override
    public  List<CategoryEntity> queryLel2Catagory(Long pid) {
        List<CategoryEntity> categoryEntities = categoryMapper.queryCategoryByPid(pid);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryAllCategoriesByCid3(Long cid3) {
        //查询三级分类
        CategoryEntity lvl3categoryEntity = this.categoryMapper.selectById(cid3);

        //查询二级分类
        CategoryEntity lvl2categoryEntity = this.categoryMapper.selectById(lvl3categoryEntity.getParentId());

        //查询一级分类
        CategoryEntity lvl1categoryEntity = this.categoryMapper.selectById(lvl2categoryEntity.getParentId());

        return Arrays.asList(lvl3categoryEntity,lvl2categoryEntity,lvl1categoryEntity);
    }


}