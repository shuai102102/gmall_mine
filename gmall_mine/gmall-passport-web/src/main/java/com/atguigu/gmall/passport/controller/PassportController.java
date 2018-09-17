package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.UserInfoService;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class PassportController {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartService cartService;

    @RequestMapping("goToLogin")
    public String login(HttpServletRequest request, String originUrl, ModelMap map){

        map.put("originUrl",originUrl);
        return "login";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request,String token ,String currentIp){

        //验证证书
        Map userMap = JwtUtil.decode("atguigu0228", token, currentIp);

        if(userMap!= null){
            //验证过期时间
            String userId = userMap.get("userId").toString();
            boolean b = userInfoService.verify(userId);
            if(!b){
                return "fail";
            }
        }else {
            return "fail";
        }
        return "success";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo){

        //调用user的服务验证用户名和密码
        UserInfo login = userInfoService.login(userInfo);

        if(login == null){
            //用户名或密码错误
            return "username or password error";
        }else {
            //登录成功
            //颁发token
            //返回token给页面
            String ip =getIp(request);
            Map<Object,Object> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("userId",login.getId());
            stringStringHashMap.put("nickName",login.getNickName());
            String token = JwtUtil.encode("atguigu0228",stringStringHashMap,ip);

            //调用一下购物车合并的业务
            String cartListCookie = CookieUtil.getCookieValue(request,"cartListCookie",true);
            List<CartInfo> cartList = null;
            if(StringUtils.isNotBlank(cartListCookie)){
                cartList = JSON.parseArray(cartListCookie, CartInfo.class);
            }
            //合并购物车
            cartService.mergeCart(login.getId(),cartList);
            //清空登录前cookie中的购物车
            CookieUtil.deleteCookie(request,response,"cartListCookie");
            return token;
        }
    }

    private String getIp(HttpServletRequest request) {

        String ip = "";
        ip = request.getHeader("x-forwarded-for");// 负载均衡

        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();// 原始请求ip
        }

        if(StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }

        return ip;
    }
}
