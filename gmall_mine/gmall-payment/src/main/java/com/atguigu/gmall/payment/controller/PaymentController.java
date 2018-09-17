package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("alipay/callback/return")
    public String alipayCallbackReturn(HttpServletRequest request,ModelMap map){

        String userId = (String) request.getAttribute("userId");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String queryString = request.getQueryString();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackContent(queryString);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentService.updatePayment(paymentInfo);

        //通知订单系统支付
        paymentService.sendPaymentResult(out_trade_no,trade_status,trade_no);

        //重定向到订单系统或者返回成功页面
        return "finish";
    }

    /**
     * 提交订单调用支付宝接口
     * @param request
     * @param orderId
     * @param map
     * @return
     */
    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request, String orderId, ModelMap map){

        String userId = (String) request.getAttribute("userId");
        map.put("orderId",orderId);
        OrderInfo orderInfo = orderService.getPaymentByOutTradeNo(orderId);

        // 支付宝接口参数封装
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\""+orderId+"\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":"+orderInfo.getTotalAmount()+"," +
//                "    \"subject\":\""+orderInfo.getOrderDetailList().get(0).getSkuName()+"\"," +
//                "  }");//填充业务参数
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("out_trade_no",orderId);
        stringObjectMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        stringObjectMap.put("total_amount",0.01);
        stringObjectMap.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
        alipayRequest.setBizContent(JSON.toJSONString(stringObjectMap));

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        System.out.println(form);

        //保存支付消息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setOutTradeNo(orderId);
        paymentInfo.setAlipayTradeNo("");
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentService.savePayment(paymentInfo);

        // 重定向到支付宝支付页面
        return form;

    }

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("goToChoosePayWay")
    public String goToChoosePayWay(HttpServletRequest request, String orderId, String totalAmount, ModelMap map){

        String userId = (String) request.getAttribute("userId");
        map.put("orderId",orderId);
        map.put("totalAmount",totalAmount);
        return "index";
    }
}
