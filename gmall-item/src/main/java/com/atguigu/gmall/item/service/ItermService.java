package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItermService {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private GmallWmsClient wmsClient;
    public ItemVo loadData(Long skuId) {
        //利用异步线程，可以提升1倍左右的效率
        ItemVo itemVo = new ItemVo();
        //一、异步开启第一线程
        CompletableFuture<SkuEntity> skuEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            //如果后面代码会用对象调用，那么一定要判空
            if (skuEntity == null) {
                return null;
            }
            //1根据skuId查询sku信息
            itemVo.setSkuId(skuEntity.getSpuId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setWeight(skuEntity.getWeight());
            return skuEntity;
        }, threadPoolExecutor);

        //接第一个线程的结果并行执行
        CompletableFuture<Void> cateCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            //2 查询分类信息
            ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryAllCategoriesByCid3(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);
        //接第一个线程的结果并行执行
        CompletableFuture<Void> brandCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            //3 查询品牌信息
            ResponseVo<BrandEntity> brandResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandResponseVo.getData();
            //快捷键brandEntity.nn --> if (brandEntity != null)
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);
        //接第一个线程的结果并行执行
        CompletableFuture<Void> spuCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            //4 查询spu相关信息
            ResponseVo<SpuEntity> spuResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> skuImageCompletableFuture = CompletableFuture.runAsync(() -> {
            //5 查询图片列表
            ResponseVo<List<SkuImagesEntity>> skuImageResponseVo = pmsClient.querySkuImageBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = skuImageResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);


        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            // 6.查询sku营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesItemBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);

        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            // 7.查询库存信息
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = wmsClient.queryWareSkuEntityBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            boolean isStore = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);
            itemVo.setStore(isStore);
        }, threadPoolExecutor);


        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> saleAttrsCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            // 8.查询spu所有的销售属性
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = this.pmsClient.querySaleAttrsBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
            // 9.查询sku的销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrResponseVo = this.pmsClient.querySkuAttrs(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
                System.out.println("---"+itemVo.getSaleAttr()+"---");
            }
        }, threadPoolExecutor);

        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> jsonCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            // 10.查询销售属性组合和skuId的映射关系
            ResponseVo<String> stringResponseVo = this.pmsClient.querySkuAttrValues(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkuJsons(json);
            System.out.println("---"+itemVo.getSkuJsons()+"---");
        }, threadPoolExecutor);

        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> spuDescCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            // 11.查询商品描述信息
            ResponseVo<SpuDescEntity> spuDescResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescResponseVo.getData();
            if (spuDescEntity != null) {
                String decript = spuDescEntity.getDecript();
                String[] urls = StringUtils.split(decript, ",");
                itemVo.setSpuImages(Arrays.asList(urls));
            }
        }, threadPoolExecutor);

        //另开一个线程：不需要第一个线程结果
        CompletableFuture<Void> itemGroupCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            // 12.查询组及组下的规格参数和值
            ResponseVo<List<ItemGroupVo>> itemGroupResponseVo = pmsClient.queryItemGroupByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuEntity.getSpuId(), skuId);
            List<ItemGroupVo> itemGroupVos = itemGroupResponseVo.getData();
            itemVo.setGroups(itemGroupVos);
        }, threadPoolExecutor);

        //等待所有线程都执行完毕后。才结束当前主线程
        CompletableFuture.allOf(cateCompletableFuture,brandCompletableFuture,
                spuDescCompletableFuture,spuCompletableFuture,skuEntityCompletableFuture,skuImageCompletableFuture,
                salesCompletableFuture,saleAttrsCompletableFuture,jsonCompletableFuture,itemGroupCompletableFuture,
                wareCompletableFuture).join();
        return itemVo;
    }
}
