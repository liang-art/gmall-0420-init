package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }


    ////验证手机号或用户名或邮箱是否可用
    @Override
    public Boolean check(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        //查询数量等于0说明是可用的。别人还没占用
        return this.userMapper.selectCount(wrapper) == 0;
    }


    //注册功能
    @Override
    public void register(UserEntity userEntity, String code) {
        //1 TODO:验证码验证

        //2 生成盐并保存
        String salt = UUID.randomUUID().toString().substring(0, 6);
        //生成的盐要保存，以便登录的时候再次解密
        userEntity.setSalt(salt);
        //3 密码加密
        String password = userEntity.getPassword();
        password = password + salt;
        userEntity.setPassword(DigestUtils.md5Hex(password));

        //4 设置一些默认信息
        userEntity.setCreateTime(new Date());
        userEntity.setGrowth(1000);
        userEntity.setIntegration(1000);
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setSourceType(1);
        //5保存用户
        this.save(userEntity);

        //6 TODO:删除redis中的短信验证码


    }


    //查询用户
    @Override
    public UserEntity queryUser(String loginName, String password) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<UserEntity>().eq("username", loginName).or().eq("phone", loginName).or().eq("email", loginName);
        //1根据用户名或手机号或邮箱查询用户
        UserEntity userEntity = this.getOne(wrapper);
        if (userEntity != null) {
            //2密码比较
            String salt = userEntity.getSalt();
            password = password+salt;
            String digestPwd = DigestUtils.md5Hex(password);
            String dbpassword = userEntity.getPassword();
            if(StringUtils.equals(dbpassword,digestPwd)){
                return userEntity;
            }
        }
        return null;
    }

}