package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sun.deploy.ui.AboutDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService{
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartAsyncService cartAsyncService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    //外层key前缀
    private static final String KEY_PREFIX = "cart:info:";

    //实时价格前缀
    private static final String PRICE_PREFIX="cart:price";

    public void addCarts(Cart cart){
        String userId = getUserId();
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        BigDecimal count = cart.getCount();
        //添加商品是否存在，存在则修改数量，否则添加
        if(boundHashOps.hasKey(cart.getSkuId())){
            String s = boundHashOps.get(cart.getSkuId()).toString();
            cart = JSON.parseObject(s, Cart.class);
            cart.setCount(cart.getCount().add(count));
        }else{
            cart.setUserId(userId);
            cart.setCheck(true);
            //查询sku相关
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if(skuEntity!=null){
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }

            //查询库存
            ResponseVo<List<WareSkuEntity>> wareSkuEntityVo = this.wmsClient.queryWareSkuEntityBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuEntityVo.getData();
            if(CollectionUtils.isEmpty(wareSkuEntities)){
                //  >0则表示有库存
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
            }

            //查询营销信息
            ResponseVo<List<ItemSaleVo>> ItemSaleVo = this.smsClient.querySalesItemBySkuId(cart.getSkuId());
            List<ItemSaleVo> ItemSaleVos = ItemSaleVo.getData();
            cart.setSales(JSON.toJSONString(ItemSaleVos));

            //查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueEntityVo = this.pmsClient.querySkuAttrs(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueEntityVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
        }

        boundHashOps.put(cart.getUserId(),JSON.toJSONString(cart));

    }

    /**
     * 添加购物车
     * @param cart
     * @return redis：hash数据模型。外层的key是userId/userKey 内层的key取skuId
     * 取userkey/usreId是为了保存的redis缓存中，那么userKey/userId作为hash模型的key
     */
    public void addCart(Cart cart) {//此时的cart只有：skuId,count
        String userId = getUserId();
        ////外层大key--> KEY_PREFIX+userkey
        String key = KEY_PREFIX + userId;
        //先从缓存中取购物车
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //主要：获取redis的值时，键一定都是String类型！！！！
        String skuIdStr = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();//传递过来的count
        //如果缓存中有，则从缓存中拿，然后修改数量。里层小key-->skuId.toString()
        if (hashOps.hasKey(skuIdStr)) {
            String cartJson = hashOps.get(skuIdStr).toString();
            cart = JSON.parseObject(cartJson, Cart.class);//购物车里面的cart
            //此时是在购物车原有的物品上加数量
            //因为count是BigDecimal类型的可以使用add()方法
            cart.setCount(cart.getCount().add(count));
            //更新到redis:外层key不需要指定了！！！！！
            //只需要指定：内层key,val
//            hashOps.put(skuIdStr, JSON.toJSONString(cart));
            //为了保存数据的一致性：用user_id和sku_id来作为修改字段
            //更新到mysql:未登录，那么userId就是userKey；登录了，那么userId就是userId
//            this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
            // 更新到mysql

            //通过异步的方式修改到mysql
            this.cartAsyncService.updateCart(userId, cart);
        }else{//缓存中没有，则用skuId查，然后给购物车对象赋值
            cart.setUserId(userId);
            cart.setCheck(true);
            //查询sku相关
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if(skuEntity!=null){
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }

            //查询库存
            ResponseVo<List<WareSkuEntity>> wareSkuEntityVo = this.wmsClient.queryWareSkuEntityBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuEntityVo.getData();
            if(CollectionUtils.isEmpty(wareSkuEntities)){
                //  >0则表示有库存
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
            }

            //查询营销信息
            ResponseVo<List<ItemSaleVo>> ItemSaleVo = this.smsClient.querySalesItemBySkuId(cart.getSkuId());
            List<ItemSaleVo> ItemSaleVos = ItemSaleVo.getData();
            cart.setSales(JSON.toJSONString(ItemSaleVos));

            //查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueEntityVo = this.pmsClient.querySkuAttrs(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueEntityVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            //新增到数据库
            //异步优化
            this.cartAsyncService.insertCart(userId,cart);

            //缓存中不存在，则需要实时价格到缓存中
            if(skuEntity!=null){
                this.redisTemplate.opsForValue().set(PRICE_PREFIX+skuIdStr,skuEntity.getPrice().toString());
            }
        }

        //更新到redis:外层key不需要指定了！！！！！
        //只需要指定：内层key,val
        //提取到这里
        hashOps.put(skuIdStr,JSON.toJSONString(cart));
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        //如果userId为空，说明未登录，那么就获取userKey
        if (userId == null) {
            return userInfo.getUserKey();
        }
        //不为空，那么登录了，返回userId
        return userId.toString();
    }


    /**
     * 根据skuId查询订单：用于回显
     * @param skuId
     * @return
     */
    public Cart queryCartBySkuId(Long skuId) {
        String userId = this.getUserId();
        String key = KEY_PREFIX+userId;
        BoundHashOperations<String, Object, Object> hasOps = this.redisTemplate.boundHashOps(key);
        if(hasOps.hasKey(skuId.toString())){
            String cartJson = hasOps.get(skuId.toString()).toString();
            Cart cart = JSON.parseObject(cartJson, Cart.class);
            return cart;
        }
        //否则抛异常
        throw new RuntimeException("该用户的购物车不包含该商品信息！");

    }

    public List<Cart> queryCarts() {
        //获取unLoginKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userkey = userInfo.getUserKey();
        //拼接hash的key
        String unLoginKey = KEY_PREFIX+userkey;
        //外层大key--> KEY_PREFIX+userkey
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        //通过大key获取对应的所有key value
        List<Object> cartJsons = unLoginHashOps.values();
        List<Cart> unLoginCartList=null;
        if(!CollectionUtils.isEmpty(cartJsons)){
            unLoginCartList = cartJsons.stream().map(cartJson -> {
                //在没有登录状态下，设置实时价格
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(cart.getPrice());
                return cart;
            }).collect(Collectors.toList());
        }
        //如果userId==null，那么就是没登录。直接返回未登录的购物车信息
        Long userId = userInfo.getUserId();
        if(userId==null){
            return unLoginCartList;
        }

        //登录了，那么就有登录的大key
        String loginKey = KEY_PREFIX+userId;

        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);

        if(!CollectionUtils.isEmpty(unLoginCartList)){
            unLoginCartList.forEach(cart -> {
                //如果两个状态都有同一个商品，那么登录状态数量+1，
                if(loginHashOps.hasKey(cart.getSkuId().toString())){
                    BigDecimal count = cart.getCount();
                    String loginCartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(loginCartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    //修改到redis
                    loginHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
                    //修改到数据库
//                    this.cartMapper.update(cart,new UpdateWrapper<Cart>().eq("user_id",userId).eq("sku_id",cart.getSkuId()));
                    //优化
                    this.cartAsyncService.updateCart(userId.toString(),cart);
                }else{//不包含，就要新增
                    //如果非登录状态的购物车的商品，在登录状态中没有，那么把userid设置为用户的id即可
                    //将userId改为用户的userId即可
                    cart.setUserId(userId.toString());
                    //修改到redis
                    loginHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
                    //新增到数据库
//                    this.cartMapper.insert(cart);
                    this.cartAsyncService.insertCart(userId.toString(),cart);
                }
            });
        }
        
        //删除非登录状态的购物车信息
        //redis里面删除
        this.redisTemplate.delete(unLoginKey);
        //数据库删除
//        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userkey));
        //优化
        this.cartAsyncService.deleteCartByUserId(userkey);
        List<Object> loginCartJsons = loginHashOps.values();
        if(!CollectionUtils.isEmpty(loginCartJsons)){
            List<Cart> loginCarts = loginCartJsons.stream().map(cartjson -> {
                Cart cart = JSON.parseObject(cartjson.toString(), Cart.class);
                //获取实时价格，设置给当前价格购物车，返回给购物车列表
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX+cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
            return loginCarts;
        }
        return null;
    }


    public void updateNum(Cart cart) {
        // 获取用户的登录信息
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            BigDecimal count = cart.getCount();

            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);

            // 写回数据库
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            this.cartAsyncService.updateCart(userId, cart);
        }
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        String key = KEY_PREFIX+userId;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if(hashOps.hasKey(skuId.toString())){
            //从redis缓存中删除
            hashOps.delete(skuId.toString());
            //从mysql数据库中删除
            this.cartAsyncService.deleteCartByUserIdAndSkuId(userId,skuId);
        }
    }


    //根据userId删除购物车
    public void deleteCartByUid(String userId) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userId));
    }

    //添加购物车
    public void save(Cart cart) {
       this.cartMapper.insert(cart);
    }

    /**
     * 根据用户Id查询购物车选中的商品
     * @param userId
     * @return
     */
    public List<Cart> queryCheckedCartByUserId(Long userId) {
        String key = KEY_PREFIX+userId;
        //根据KEY直接从缓存中命中
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> values = hashOps.values();
        if(!CollectionUtils.isEmpty(values)){
            return values.stream()
                    .map(cartJson->JSON.parseObject(cartJson.toString(),Cart.class))
                    //过滤选中的购物车商品
                    .filter(Cart::getCheck).collect(Collectors.toList());
        }
        return null;
    }
}
