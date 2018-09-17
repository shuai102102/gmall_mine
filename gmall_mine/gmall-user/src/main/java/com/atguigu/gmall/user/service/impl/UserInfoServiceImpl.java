package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserAddressService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> userList() {

//        List<UserInfo> userInfoList = userInfoMapper.selectAllUserInfo();
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        return userInfos;
    }

    @Override
    public List<UserInfo> userFindOne(String name) {

        UserInfo user = new UserInfo();
        user.setName(name);

        List<UserInfo> userInfos = userInfoMapper.select(user);
        return userInfos;
    }

    @Override
    public void userdel(Integer id) {
        userInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void userUpdate(UserInfo userInfo) {
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void userAdd(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        //验证用户名和密码
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);

        //成功登录，放入缓存用户信息
        //user:userId:info
        if(userInfo1 != null){
            Jedis jedis = redisUtil.getJedis();

            jedis.setex("user:"+userInfo1.getId()+":info",60*30, JSON.toJSONString(userInfo1));

            jedis.close();
        }
        //返回空值
        return userInfo1;
    }

    @Override
    public boolean verify(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String s = jedis.get("user:"+userId+":info");

        if(StringUtils.isBlank(s)){
            return false;
        }else {

            //可以获得当key的过期时间
            //jedis.ttl("user:" + userId + ":info");
            jedis.expire("user:"+userId+":info",60*30);
            return true;
        }
    }

    @Override
    public UserAddress getUserAddressByAddressId(String addressId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(addressId);
        UserAddress userAddress1 = userAddressMapper.selectOne(userAddress);
        return userAddress1;
    }

}
