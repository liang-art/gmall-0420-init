队列监听：缓存+最终一致【MQ】实现价格：修改价格时，缓存的价格也随之改变
    1、当gmall-pms-->SpuController修改价格时
    2、发送队列消息，将spuId传过来
    3、gmall-cart监听消息，根据spuId获取spu下的sku
    4、根据sku的skuId查询redis缓存价格，存在，则说明购物车的价格需要和修改的价格同步
    5、根据sku获取修改后的价格，同步到redis缓存中去。