package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {

        paymentInfoMapper.insert(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());

        paymentInfoMapper.updateByExampleSelective(paymentInfo,e);
    }

    @Override
    public void sendPaymentResult(String out_trade_no, String trade_status, String trade_no) {

        Connection connection = activeMQUtil.getConnection();
        try {
            // 获得mq连接
            connection.start();

            // 创建mq的执行会话
            // 第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

            // 创建消息对象
            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",out_trade_no);
            mapMessage.setString("trade_status",trade_status);
            mapMessage.setString("trade_no",trade_no);
            //producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // 通过消息对象提交消息内容
            producer.send(mapMessage);

            // 提交会话
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
