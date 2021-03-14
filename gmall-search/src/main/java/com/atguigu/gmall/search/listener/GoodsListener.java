package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodRepsitory;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {
    @Autowired
    private GoodRepsitory repository;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallPmsClient pmsClient;

    //Ctrl+Shift+U 快捷键
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH-SAVE-QUEUE",durable = "true"),
            exchange = @Exchange(value = "SEARCH-EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"search.save"}
    ))
    public void saveGoods(Long spuId, Channel channel, Message message){

        // 查询出spu下所有的sku集合                                              替换为spuId
        ResponseVo<List<SkuEntity>> skusResponseVo = this.pmsClient.querySkuBySpuid(spuId);
        List<SkuEntity> skuEntities = skusResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)){
            // 1、转化成goods集合  skuEntities集合转化为goods集合
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();

                // sku相关信息
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setDefaultImage(skuEntity.getDefaultImage());

                // 品牌相关信息
                ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }

                // 分类相关信息
                ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                //根据spuId远程调用,获取spuEntity
                ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(spuId);
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                // spu相关信息
                goods.setCreateTime(spuEntity.getCreateTime());

                // 库存相关信息
                ResponseVo<List<WareSkuEntity>> wareSkusResponseVO = this.wmsClient.queryWareSkuEntityBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareSkusResponseVO.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)){
                    //anyMatch判断store是 boolean类型
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                    //reduce对map的处理结果进行求和
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                }

                // 检索属性和值
                List<SearchAttrValueVo> attrValueVos = new ArrayList<>();
                // skuAttrValueEntity集合 转化为 searchAttrValueVo集合
                ResponseVo<List<SkuAttrValueEntity>> skuAttrsResponseVo = this.pmsClient.querySearchSkuAttrValuebyCidAndSkuId(skuEntity.getCatagoryId(), skuEntity.getId());
                List<SkuAttrValueEntity> searchSkuAttrValueEntities = skuAttrsResponseVo.getData();
                if (!CollectionUtils.isEmpty(searchSkuAttrValueEntities)){
                    attrValueVos.addAll(searchSkuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }

                // spuAttrValueEntity集合 searchAttrValueVo集合
                ResponseVo<List<SpuAttrValueEntity>> spuAttrsResponseVo = this.pmsClient.querySearchSpuAttrValueByCidAndSpudId(skuEntity.getCatagoryId(), spuEntity.getId());
                List<SpuAttrValueEntity> searchSpuAttrValueEntities = spuAttrsResponseVo.getData();
                if (!CollectionUtils.isEmpty(searchSpuAttrValueEntities)){
                    attrValueVos.addAll(searchSpuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }
                goods.setSearchAttrs(attrValueVos);

                return goods;
            }).collect(Collectors.toList());

            // 批量导入到es
            this.repository.saveAll(goodsList);
        }
    }

}
