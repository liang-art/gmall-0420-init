package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "stock:lock:";
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     *
     * 1、验库存：查询是否有库存：stock-stock_locked
     * 2、锁库存：update stock_locked
     * 3、将锁库存信息保存的redis里面，方便以后解库存
     * @param lockVos
     * @param orderToken
     * @return
     */
    @Transactional //分布式事务，被调用者加事务
    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {

        if(CollectionUtils.isEmpty(lockVos)){
            throw new OrderException("没有选中的商品记录");
        }

        //遍历所有商品，验库存，并锁定库存
        lockVos.forEach(lockVo -> {
            this.checkLock(lockVo);
        });

        // 只要有一个锁定失败，都应该解锁所有锁定成功的商品的库存信息:即把库存加回去
        if (lockVos.stream().anyMatch(lockVo->!lockVo.getLock())) {
            // 获取锁定成功的商品列表
            List<SkuLockVo> successLockVos = lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
            successLockVos.forEach(lockVo -> {
                this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());
            });
            return lockVos;
        }

        //把锁定库存信息保存到redis，方便将来解锁库存 和 减库存
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos));


        //为了防止宕机导致死锁情况
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE","stock.ttl",orderToken);
        // 返回值为null，代表都锁定成功
        return null;
    }


    /**
     * 验证库存：查询库存是否充足
     * 锁库存：修改stock字段，减去用户买的数量
     * 为了让查库存和锁库存保证原子性，给他们加分布式锁。
     *
     *
     * @param lockVo
     */
    private void checkLock(SkuLockVo lockVo) {
        RLock fairLock = this.redissonClient.getFairLock("stock:" + lockVo.getSkuId());
        fairLock.lock();
        //：让查询和修改库存具有原子性，使用Redisson
        //查库存
        List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.check(lockVo.getSkuId(),lockVo.getCount());
        if(CollectionUtils.isEmpty(wareSkuEntities)){//没有库存
            lockVo.setLock(false);//如果没有仓库满足，则锁定失败
            fairLock.unlock();//解锁
            return;//此方法结束
        }

        //执行到此处，说明库存足够
        //锁库存:此处默认取第一个
        WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
        Long id = wareSkuEntity.getId();
        //修改锁库存字段成功。
        if(this.wareSkuMapper.lock(id,lockVo.getCount())==1){
            lockVo.setLock(true);//锁定库存成功，则将状态设置为true
            lockVo.setWareSkuId(id);//记录锁的是哪一个库存。以便解锁指定库存。
        }

        fairLock.unlock();
    }

}