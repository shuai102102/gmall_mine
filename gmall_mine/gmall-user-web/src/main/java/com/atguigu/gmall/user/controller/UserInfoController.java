package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserInfoController {

    @Reference
    UserInfoService userService;

    @RequestMapping("userList")
    public ResponseEntity<List<UserInfo>> userList(){

        List<UserInfo> userInfoList = userService.userList();
        return ResponseEntity.ok(userInfoList);
    }

    @RequestMapping("userListCondition")
    public ResponseEntity<List<UserInfo>> userFindOne(@RequestParam(value = "condition",required = false) String condition) {
        List<UserInfo> userInfoList = new ArrayList<>();
        if(!StringUtils.isEmpty(condition)) {
            userInfoList = userService.userFindOne(condition);
        }
        return ResponseEntity.ok(userInfoList);
    }

    @RequestMapping("userDel")
    public ResponseEntity<List<UserInfo>> userDel(@RequestParam(value = "id") Integer id) {

        userService.userdel(id);

        return ResponseEntity.ok(null);
    }

    @RequestMapping("userUpdate")
    public ResponseEntity<List<UserInfo>> userUpdate(UserInfo userInfo) {
        if(userInfo != null) {
            userService.userUpdate(userInfo);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping("userAdd")
    public ResponseEntity<List<UserInfo>> userAdd(UserInfo userInfo) {
        if(userInfo != null) {
            userService.userAdd(userInfo);
        }
        return ResponseEntity.ok(null);
    }
}
