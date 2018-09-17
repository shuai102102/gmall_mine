package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    List<UserInfo> userList();

    List<UserInfo> userFindOne(String name);

    void userdel(Integer id);

    void userUpdate(UserInfo userInfo);

    void userAdd(UserInfo userInfo);

    UserInfo login(UserInfo userInfo);

    boolean verify(String userId);

    UserAddress getUserAddressByAddressId(String addressId);
}
