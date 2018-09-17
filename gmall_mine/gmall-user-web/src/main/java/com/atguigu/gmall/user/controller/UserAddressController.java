package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserAddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

    @RestController
    public class UserAddressController {

        @Reference
        UserAddressService userAddressService;

        @RequestMapping ("userAdList")
        public ResponseEntity<List<UserAddress>> userListALl(){

            List<UserAddress> userAddressList = userAddressService.userAdList();

            return ResponseEntity.ok(userAddressList);
        }

        @RequestMapping("userAdFindOne")
        public ResponseEntity<List<UserAddress>> useraAdFindOne(@RequestParam(value="userId")String userId){

            List<UserAddress> userAddressList = userAddressService.userAdFindOne(userId);

            return ResponseEntity.ok(userAddressList);
        }

        @RequestMapping("userAdDel")
        public ResponseEntity<List<UserAddress>> userAdDel(@RequestParam(value="id")Integer id){

            userAddressService.userAdDel(id);

            return ResponseEntity.ok(null);
        }

        @RequestMapping("userAdUpdata")
        public ResponseEntity<List<UserAddress>> userAdUpdata(UserAddress userAddress){

            userAddressService.userAdUpdata(userAddress);

            return ResponseEntity.ok(null);
        }

        @RequestMapping("userAdAdd")
        public ResponseEntity<List<UserAddress>> userAdAdd(UserAddress userAddress){

            userAddressService.userAdAdd(userAddress);

            return ResponseEntity.ok(null);
        }
}
