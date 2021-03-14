package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CartMQListener {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX="cart:price";
    //监听消息
    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value = "price_queue",durable = "true"),
            exchange = @Exchange(value = "PRICE_EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key={"update.price"}
    ))
    public void listenerPriceUpdate(Long spuId, Channel channel, Message message) throws IOException {
        //如果spuId为null
        if(spuId==null){
            //直接消费信息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return ;
        }
        //根据spuId查询spu下的所有sku
        ResponseVo<List<SkuEntity>> listResponseVo = pmsClient.querySkuBySpuid(spuId);
        List<SkuEntity> skuEntities = listResponseVo.getData();
        if(!CollectionUtils.isEmpty(skuEntities)){
            skuEntities.forEach(skuEntity -> {
                //根据skuId从缓存中查询相应的缓存价格
                String price = redisTemplate.opsForValue().get(PRICE_PREFIX + skuEntity.getId());
                //如果缓存中的价格不为空。那么修改的价格在缓存中有
                if(StringUtils.isNotBlank(price)){
                    //那么把修改的价格同步到缓存价格中去
                    this.redisTemplate.opsForValue().set(PRICE_PREFIX+skuEntity.getId(),skuEntity.getPrice().toString());
                }
            });
        }

    }



    //监听消息,删除购物车
    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value = "CART_DELETE_QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key={"cart.delete"}
    ))
    public void delete(Map<String,Object> map,Channel channel,Message message) throws IOException {
        //如果消息为空，消息直接消费
        if(CollectionUtils.isEmpty(map)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        //获取用户Id
        String userId = map.get("userId").toString();
        //获取skuIds
        String skuIds = map.get("skuIds").toString();
        //反序列化
        List<String> skuIdList = JSON.parseArray(skuIds, String.class);
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        //从缓存中删除对应的购物车商品
        hashOps.delete(skuIdList.toArray());

        //从数据库中删除购物车对应商品
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userId).in("sku_id",skuIdList));
        //TODO:重试rejectAck
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
