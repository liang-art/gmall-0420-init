package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName:GmallPmsApi
 * Package:com.atguigu.gmall.pms.api
 * Date:2020/9/27 19:14
 *
 * @Author:com.bjpowernode
 */

public interface GmallPmsApi {
    //==================gmall-item模块所需接口======================================

    //1 根据SkuId查询sku信息
    @GetMapping("pms/sku/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    //2 根据三级分类Id查询一二三级分类
    @GetMapping("pms/category/all/{cid3}")
    public ResponseVo<List<CategoryEntity>> queryAllCategoriesByCid3(@PathVariable("cid3")Long cid3);


    //3.根据brandId查询brand:下面有了

    //4.根据spuId查询spu：下面有了

    //5根据skuId查询sku的图片列表
    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>>  querySkuImageBySkuId(@PathVariable("skuId")Long skuId);

    //8.根据spuId查询spu下所有sku的销售属性
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrsBySpuId(@PathVariable("spuId")Long spuId);

    //9.根据skuId查询sku的销售属性
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrs(@PathVariable("skuId")Long skuId);

    //10根据spuId查询spu下所有sku的销售属性组合和skuId的映射关系 Y
    @GetMapping("pms/skuattrvalue/sku/mapping/{spuId}")
    public ResponseVo<String> querySkuAttrValues(@PathVariable("spuId")Long spuId);

    //11根据spuId查询商品描述信息
    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    //12. 根据categoryId、spuId、skuId查询组信息及组下的规格参数和值
    @GetMapping("pms/attrgroup/with/attr/value/{categoryId}")
    public ResponseVo<List<ItemGroupVo>> queryItemGroupByCidAndSpuIdAndSkuId(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId
    );

    //===================gmall-item模块所需接口=====================================
    //pms-index模块需要：二级和三级分类信息
    @GetMapping("pms/category/parent/sub/{pid}")
    @ApiOperation("查询二级和三级分类信息")
    public  ResponseVo<List<CategoryEntity>> queryLel2Catagory(@PathVariable("pid") Long pid);

    //pms-index模块需要根据父parentId查询分类信息
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentId")Long pid);


    //
    //1 分页查询spu
    @PostMapping("pms/spu/search")
    public ResponseVo<List<SpuEntity>> searchSpuByPage(@RequestBody PageParamVo paramVo);

    //2 根据spuid查询spu下的所有的sku
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuid(@PathVariable("spuId") Long spuId);

    //3 根据brandId查询所有品牌信息
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //4 根据catagoryId查询分类信息
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //5 根据cid和skuId查询销售属性信息
    @GetMapping("pms/skuattrvalue/search/{cid}/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValuebyCidAndSkuId(@PathVariable("cid") Long cid, @PathVariable("skuId") Long skuId);
    //6 根据cid和skuId查询基本属性信息
    @GetMapping("pms/spuattrvalue/search/{cid}/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchSpuAttrValueByCidAndSpudId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);

    //搜索：部分的rabbitmq添加：
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);
}
