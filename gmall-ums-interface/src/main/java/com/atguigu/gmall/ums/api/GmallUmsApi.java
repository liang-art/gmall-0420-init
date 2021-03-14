package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ClassName:GmallUmsApi
 * Package:com.atguigu.gmall.ums.api
 * Date:2020/10/13 20:11
 *
 * @Author:com.bjpowernode
 */

public interface GmallUmsApi {
    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(@RequestParam("loginName")String loginName, @RequestParam("password")String password);


    //根据用户Id查询地址列表
    @GetMapping("ums/useraddress/queryAddress/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryAddress(@PathVariable("userId")Long userId);

    //根据用户ID查询用户细信息
    @GetMapping("ums/user/{id}")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);
}
