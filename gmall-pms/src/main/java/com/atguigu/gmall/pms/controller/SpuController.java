package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.config.RabbitMQConfig;
import com.atguigu.gmall.pms.vo.SpuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * spu信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2020-09-21 20:23:51
 */
@Api(tags = "spu信息管理")
@RestController
@RequestMapping("pms/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    //http://api.gmall.com/pms/spu/category/0?t=1600774307011&pageNum=1&pageSize=10&key=
    //分页查询 商品集合单元
    //根据分类信息查询商品信息
    //为什么不加@RequestBody ？
    @ApiOperation("spu商品信息查询")
    @RequestMapping("category/{categoryId}")
    public ResponseVo<PageResultVo> queryPageSpu(@PathVariable("categoryId") Long cid,PageParamVo pageParamVo){
        PageResultVo querySpuPage = this.spuService.querySpuPage(cid,pageParamVo);
        return ResponseVo.ok(querySpuPage);
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySpuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = spuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }

    @ApiOperation("分页查询")
    @PostMapping("search")
    public ResponseVo<List<SpuEntity>> searchSpuByPage(@RequestBody PageParamVo paramVo){
        PageResultVo pageResultVo = spuService.queryPage(paramVo);
        return ResponseVo.ok((List<SpuEntity>)pageResultVo.getList());
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id){
		SpuEntity spu = spuService.getById(id);

        return ResponseVo.ok(spu);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SpuVo spu){
		spuService.bigSave(spu);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SpuEntity spu){
		spuService.updateById(spu);
        this.rabbitTemplate.convertAndSend("PRICE_EXCHANGE","update.price",spu.getId());
        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		spuService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
