package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Calendar;

@Component
public class PaymentSuccessListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage){
        //consumPaymentResult

        try {
            String out_trade_no = mapMessage.getString("out_trade_no");
            String trade_status = mapMessage.getString("trade_status");
            String trade_no = mapMessage.getString("trade_no");

            //根据支付结果更新订单状态
            System.out.println("监听器执行订单更新服务");
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderStatus("订单已支付");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,3);
            orderInfo.setExpireTime(c.getTime());
            orderInfo.setTrackingNo(trade_no);
            orderInfo.setProcessStatus("订单已支付");
            orderService.updateOrder(orderInfo);

            //发送订单的支付信息
            orderService.sentOrderResult(out_trade_no);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
