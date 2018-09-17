package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;

import java.util.List;

public interface UserAddressService {
    List<UserAddress> userAdList();

    List<UserAddress> userAdFindOne(String userId);

    void userAdDel(Integer id);

    void userAdUpdata(UserAddress userAddress);

    void userAdAdd(UserAddress userAddress);
}
