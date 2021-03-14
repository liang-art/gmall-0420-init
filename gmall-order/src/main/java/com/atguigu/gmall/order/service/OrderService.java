package com.atguigu.gmall.order.service;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallUmsClient umsClient;


    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final String KEY_PREFIX = "order:token:";

    //确认订单
    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if(userInfo==null) {
            return null;
        }
        //获取用户ID
        Long userId = userInfo.getUserId();


        //根据用户Id查询:用户地址列表
        ResponseVo<List<UserAddressEntity>> userAddressVo = umsClient.queryAddress(userId);
        //设置用户地址
        List<UserAddressEntity> userAddressEntities = userAddressVo.getData();
        if(!CollectionUtils.isEmpty(userAddressEntities)){
            confirmVo.setAddresses(userAddressEntities);
        }

        //根据用户ID:查询选中的购物车
        ResponseVo<List<Cart>> cartVo = cartClient.queryCheckedCartByUserId(userId);
        List<Cart> carts = cartVo.getData();
        if(CollectionUtils.isEmpty(carts)){
            throw new OrderException("您没有选中商品");
        }
        List<OrderItemVo> orderItemVos;
        orderItemVos = carts.stream().map(cart -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setCount(cart.getCount());
            orderItemVo.setSkuId(cart.getSkuId());
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            //根据购物车的skuId设置sku相关信息
            if (skuEntity != null) {
                orderItemVo.setPrice(skuEntity.getPrice());
                orderItemVo.setTitle(skuEntity.getTitle());
                orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
                orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
            }
            //根据购物车的skuId设置库存信息
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkuEntityBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                System.out.println(wareSkuEntities.toString());
            }

            //根据购物车的skuId设置sku属性信息
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueVo = this.pmsClient.querySkuAttrs(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                orderItemVo.setSaleAttrs(skuAttrValueEntities);
            }

            //根据购物车的skuId设置
            ResponseVo<List<ItemSaleVo>> itemSaleVo = this.smsClient.querySalesItemBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSaleVo.getData();
            orderItemVo.setSales(itemSaleVos);
            return orderItemVo;
        }).collect(Collectors.toList());
        confirmVo.setItems(orderItemVos);

        //根据用户ID:设置积分信息
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        if(userEntity!=null){
            confirmVo.setBounds(userEntity.getIntegration());
        }

        //自动生成orderToken保存到redis缓存中：将生成的orderToken存入redis
        //用于防止重复提交
        String orderToken = IdWorker.getTimeId();
        this.redisTemplate.opsForValue().set(KEY_PREFIX+orderToken,orderToken);
        confirmVo.setOrderToken(orderToken);
        return confirmVo;
    }

    /**
     * 提交订单：
     *  1、防止重复提交：orderToken，直接查询redis缓存
     *  2、验总价：查询数据库，已经提供好了
     *  3、验库存并锁库存：需要提供接口：gmall-wms；
     *          验库存，查询库存是否够用。锁库存：更新stocked字段；查询和更新之间要具备原子性【分布式锁】
     *  4、新增订单:需要提供接口
     *  5、删除购物车对应的商品：需要提供接口
     * @param orderSubmitVo
     * @return
     */
    public OrderEntity subimt(OrderSubmitVo orderSubmitVo) {
        String orderToken = orderSubmitVo.getOrderToken();
        //判空
        if(StringUtils.isEmpty(orderToken)){
            throw new OrderException("非法提交");
        }

        //1、防重：先查后删
        // 查询redis中是否有该token，有就把他删了。
        // 当他再次提交的时候，就会报避免重复提交
        /**
         * 用lua脚本保证查询和删除的一致性。
         */
        String script = "if(redis.call('get', KEYS[1])==ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken),orderToken);
        if(!flag){
           throw new OrderException("请不要重复提交");
        }

        //2、验总价
        //获取页面总价
        BigDecimal totalPrice = orderSubmitVo.getTotalPrice();
        //获取商品
        List<OrderItemVo> items = orderSubmitVo.getItems();
        orderSubmitVo.getItems();
        if(CollectionUtils.isEmpty(items)){
            throw new OrderException("没有被选中的商品");
        }
        //获取实时总价
      BigDecimal currTotalPrice = items.stream().map(itemVo -> {
          //遍历商品，对所有商品都进行求和
          //从数据库中获取实时价格
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(itemVo.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(itemVo.getCount());
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();
        //页面总价和实时总价进行对比。不等，则抛异常
        if(totalPrice.compareTo(currTotalPrice)!=0){
            throw new OrderException("当前页面价格已经失效，请刷新后重试");
        }

        //3、验库存并锁定库存   存在分布式事务问题【@Transactional】，即涉及到修改数据库了。
        // 如果有失败的情况，要考虑恢复数据
        List<SkuLockVo> skuLockVos = items.stream().map(item -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setCount(item.getCount().intValue());
            return skuLockVo;
        }).collect(Collectors.toList());
        //调用写好的接口，验证库存
        ResponseVo<List<SkuLockVo>> listResponseVo = this.wmsClient.checkAndLock(skuLockVos, orderToken);
        List<SkuLockVo> skuLockVoList = listResponseVo.getData();
        if(!CollectionUtils.isEmpty(skuLockVoList)){
            //如果能查到值，那么说明没锁成功
            throw new OrderException(JSON.toJSONString(skuLockVoList));
        }

        //如果锁库存成功后，服务器宕机
        /**
         * 1、用户一直不支付：定时关单
         * 2、锁定库存成功，还没新增订单。服务器宕机：定时解锁库存
         * 解决：延时队列+死信队列
         */

//        int i =10/0;
        OrderEntity orderEntity=null;
        Long userId=null;
        try {
            //4、下单   存在分布式事务问题 【@Transactional】
            UserInfo userInfo = LoginInterceptor.getUserInfo();
             userId = userInfo.getUserId();
            //传递orderSubmitVo和userId保存订单
            ResponseVo<OrderEntity> orderEntityResponseVo = this.omsClient.saveOrder(orderSubmitVo, userId);
             orderEntity = orderEntityResponseVo.getData();
        } catch (Exception e) {
            e.printStackTrace();
            //当保存订单失败时，标记订单为无效订单。并里面解锁库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE","order.disable",orderToken);
            //只要订单出错，我就立马解锁库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE","stock.unlock",orderToken);

        }

        //5、删除购物车中对应的商品：异步
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        String skuIdsStr = JSON.toJSONString(skuIds);
        map.put("skuIds",skuIdsStr);
//        rabbitTemplate.convertAndSend("ORDER_EXCHANGE","cart.delete",map);

        return orderEntity;

    }
}
