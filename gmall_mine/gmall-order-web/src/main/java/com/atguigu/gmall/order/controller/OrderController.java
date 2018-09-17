package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserInfoService userInfoService;

    @Reference
    OrderService orderService;

    @Reference
    UserAddressService userAddressService;

    @Reference
    SkuInfoService skuInfoService;

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map){

        String userId = (String) request.getAttribute("userId");

        //调用购物车服务，根据userId，获取购物车列表
        List<CartInfo> cartList = cartService.getCartListCheckedCacheByUser(userId);

        List<OrderDetail> orderDetails = new ArrayList<>();

        for (CartInfo cartInfo : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetails.add(orderDetail);
        }

        List<UserAddress> addressListByUser = userAddressService.userAdFindOne(userId);

        map.put("orderDetailList",orderDetails);
        map.put("userAddressList",addressListByUser);
        map.put("totalAmount",getTotalPrice(cartList));

        //生成交易码
        String tradeCode = orderService.genTradeCode(userId);

        //页面存入交易码
        map.put("tradeCode",tradeCode);

        return "trade";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartList) {

        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();

            if(isChecked.equals("1")){
                BigDecimal cartPrice = cartInfo.getCartPrice();

                totalPrice = totalPrice.add(cartPrice);
            }
        }
        return totalPrice;
    }

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String> paramMap, ModelMap map){

        String userId = (String) request.getAttribute("userId");
        String tradeCode = paramMap.get("tradeCode");
        String addressId = paramMap.get("addressId");

        //验证交易码
        boolean b = orderService.checkTradeCode(userId,tradeCode);
        if (b){
            List<String> cartIds = new ArrayList<>();
            List<CartInfo> cartList = cartService.getCartListCheckedCacheByUser(userId);
            //封装订单信息
            OrderInfo orderInfo = new OrderInfo();
            List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();

            for (CartInfo cartInfo : cartList) {
                OrderDetail orderDetail = new OrderDetail();
                //验价和验库存
                boolean ifPrice = skuInfoService.checkSkuPrice(cartInfo);
                if(ifPrice){
                    //封装订单数据
                    if(ifPrice){
                        //封装订单数据
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetail.setSkuName(cartInfo.getSkuName());
                        orderDetail.setSkuId(cartInfo.getSkuId());
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                        orderDetail.setImgUrl(cartInfo.getImgUrl());
                        orderDetails.add(orderDetail);
                        cartIds.add(cartInfo.getId());
                    }else {
                        map.put("errMsg","订单交易失效（库存或者价格发生变动，请重新下单）");
                        return "tradeFail";
                    }
                }
            }
            UserAddress userAddress = userInfoService.getUserAddressByAddressId(addressId);
            orderInfo.setOrderDetailList(orderDetails);
            //日期格式化，封装外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
            String format = sdf.format(new Date());
            String outTradeNo = "ATGUIGU" + format + System.currentTimeMillis();
            //全局订单单号
            orderInfo.setOutTradeNo(outTradeNo);

            orderInfo.setOrderStatus("未付款");
            orderInfo.setProcessStatus("未付款");
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());
            orderInfo.setCreateTime(new Date());

            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            //日期加减，封装过期时间或者预计送达时间
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            orderInfo.setExpireTime(c.getTime());
            orderInfo.setOrderComment("硅谷订单，没有商品可送");
            orderInfo.setTotalAmount(getTotalPrice(cartList));
            orderInfo.setUserId(userId);
//            orderInfo.setPaymentWay(PaymentWay.ONLINE);

            //将订单保存到数据库
            orderService.saveOrder(orderInfo);
            //清理购物车
            cartService.cleanCart(cartIds,userId);
            //重定向到支付系统
            return "redirect:http://payment.gmall.com:8087/goToChoosePayWay?orderId="+outTradeNo+"&totalAmount="+orderInfo.getTotalAmount();

        }else {
            map.put("errMsg","订单交易失败");
            return "tradeFail";
        }
    }
}
