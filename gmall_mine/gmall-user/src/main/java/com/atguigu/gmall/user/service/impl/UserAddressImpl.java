package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;



import java.util.List;

@Service
public class UserAddressImpl implements UserAddressService{

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> userAdList() {

        List<UserAddress> userAddressList = userAddressMapper.selectAll();
        return userAddressList;
    }

    @Override
    public List<UserAddress> userAdFindOne(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        List<UserAddress> userAddresses = userAddressMapper.select(userAddress);

        return userAddresses;
    }

    @Override
    public void userAdDel(Integer id) {
        userAddressMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void userAdUpdata(UserAddress userAddress) {
        userAddressMapper.updateByPrimaryKeySelective(userAddress);
    }

    @Override
    public void userAdAdd(UserAddress userAddress) {
        userAddressMapper.insert(userAddress);
    }
}