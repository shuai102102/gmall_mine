package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuInfoService;


import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Reference
    SkuInfoService skuInfoService;

    @Reference
    CartService cartService;

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(){
        return "tradeTest";
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request,HttpServletResponse response,CartInfo cartInfo,ModelMap map){

        String userId = "2";
        List<CartInfo> cartList = new ArrayList<>();
        if(StringUtils.isBlank(userId)){
            // 更新cookie
            // 获得cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if(StringUtils.isNotBlank(cartListCookie)){
                List<CartInfo> cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
                for (CartInfo info : cartInfos) {
                    if(info.getSkuId().equals(cartInfo.getSkuId())){
                        info.setIsChecked(cartInfo.getIsChecked());
                    }
                }

            }else {
                //如果cookie为空什么也不做
            }

        }else {
            //更新Db和同步redis
            //修改DB中的选中状态
            cartInfo.setUserId(userId);
            cartService.updateCartChecked(cartInfo);
            //同步redis
            cartList = cartService.cartCache(userId);

        }

        BigDecimal totalPrice = getTotalPrice(cartList);

        map.put("totalPrice",totalPrice);
        map.put("cartList",cartList);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequire(isNeededSuccess = false)
    public String cartList(HttpServletRequest request, ModelMap modelMap){
        String userId = "2";
        List<CartInfo> cartList = new ArrayList<>();
        //判断是否登录
        if(StringUtils.isBlank(userId)){
            //未登录，查询Cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                cartList = JSON.parseArray(cartListCookie,CartInfo.class);
            }
        }else {
            //已登录，查询DB（redis）
            cartList = cartService.getCartListCacheByUser(userId);
        }

        BigDecimal totalPrice = getTotalPrice(cartList);

        modelMap.put("totalPrice",totalPrice);
        modelMap.put("cartList",cartList);

        return "cartList";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartList) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            //检查购物车是否被选中
            String isChecked = cartInfo.getIsChecked();
            if("1".equals(isChecked)){
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String,String> map){
        //声明购物车对象
        List<CartInfo> cartInfos = new ArrayList<>();
        String userId = (String)request.getAttribute("userId");
        String skuId = map.get("skuId");
        String num = map.get("num");

        //根据skuId查询对应的商品信息，封装购物车参数对象
       CartInfo cartInfo = getCartInfoBySkuId(skuId,num);

       if(StringUtils.isBlank(userId)){
           // 用户未登陆，操作浏览器cookie
           String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
           if(StringUtils.isBlank(cartListCookie)){
                //cookie中没有购物车数据
               cartInfos.add(cartInfo);
           }else {
               //cookie中有购物车数据
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
                //判断是否是新的购物车，从而决定是添加还是修改
                boolean b =  idNewCart(cartInfos,cartInfo);
               if(b){
                   //新车，添加
                   cartInfos.add(cartInfo);
               }else {
                   //老车，修改
                   for (CartInfo info : cartInfos) {
                       if (info.getSkuId().equals(cartInfo.getSkuId())){
                           info.setSkuNum(info.getSkuNum() + Integer.parseInt(num));
                           BigDecimal multiply = info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum()));
                           info.setCartPrice(multiply);
                       }
                   }
               }
           }
           // 覆盖浏览器cookie
           CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(cartInfos),60*5,true);
       }else {
           // 用户已经登陆，操作db和redis
           // 使用userId和skuId查询当前的购物车商品是否曾经添加过
           CartInfo cartDb = cartService.ifCartExist(userId,skuId);
           if(cartDb!=null){
               //数据库中存在此购物车，更新购物车
               cartInfo.setId(cartDb.getId());
               cartInfo.setSkuNum(cartDb.getSkuNum()+Integer.parseInt(num));
               cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
               cartService.addCart(cartInfo);
           }else {
                //数据库中没有此购物车，添加购物车
               cartInfo.setUserId(userId);
               cartService.addCart(cartInfo);
           }
           //同步缓存
           cartService.cartCache(userId);
       }


        return "redirect:http://cart.gmall.com:8084/cartSuccess";
    }

    private boolean idNewCart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = false;
        for (CartInfo info : cartInfos) {
            if (info.getSkuId().equals(cartInfo.getSkuId())){
                b = true;
                break;
            }
        }
        return b;
    }


    private CartInfo getCartInfoBySkuId(String skuId, String num) {
        CartInfo cartInfo = new CartInfo();
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        // 设置购物车的skuId
        cartInfo.setSkuId(skuId);
        // 设置购物车的添加数量
        cartInfo.setSkuPrice(skuInfo.getPrice());
        // 设置购物车的添加数量
        cartInfo.setSkuNum(Integer.parseInt(num));
        // 设置购物车的价格
        BigDecimal multiply = cartInfo.getSkuPrice().multiply(new BigDecimal(num));
        cartInfo.setCartPrice(multiply);
        // 设置购物车其他信息
        cartInfo.setIsChecked("1");
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());

        return cartInfo;
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("cartSuccess")
    public String cartSuccess(){

        return "success";
    }
}
