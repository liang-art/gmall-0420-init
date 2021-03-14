package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import jdk.internal.util.xml.impl.Attrs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {


    @Autowired
    private AttrService attrService;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySearchSpuAttrValueByCidAndSpudId(Long cid, Long spuId) {
        List<AttrEntity> attrEntities = attrService.list(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));
        if(CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        List<Long> ids = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        List<SpuAttrValueEntity> spuAttrValueEntities = this.list(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id", spuId).in("attr_id", ids));

        return spuAttrValueEntities;
    }



}