一、订单确认页面所需接口：
数据接口：
1、根据用户Id查询用户的地址列表 gmall-ums-->address
2、根据用户Id查询用户的基本信息 gmall-ums->user
3、根据用户Id查询用户选中的购物信息（skuId,count）
4、根据skuId查询sku信息
5、根据skuId查询sku的销售属性
6、根据skuId查询sku的库存信息
7、根据skuId查询sku的营销信息



二、提交订单
1、防止重复提交：在订单确认逻辑中生成前缀+订单号【生成IdWorker.getTimeId()】存入redis
                此时用lua脚本从Redis中先查询，然后再删除。查询到则删除，查不到则报不要重复提交


2、验总价：即从订单页获取的总价和从数据库中查询的总价进行对比。


3、验库存并锁库存：先查询MySQL数据库中是否有足够库存，有则锁库存【查询和修改要保证他们两同时成功或失败，用分布式锁Redisson】
    验库存：即查询库存是否充足够用
    锁库存：即修改stock_locked字段 加上用户购买的数量 【因为是stock_locked是锁库存字段】
    只要有一个锁定失败，那么就解锁所有库存
4、新增订单

5、删除购物车对应的商品信息【rabbitMQ异步删除】