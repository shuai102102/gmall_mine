package com.atguigu.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestBoss {

    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://192.168.186.102:61616");
        try {

            // 获得mq连接
            Connection connection = connect.createConnection();
            connection.start();


            // 创建mq的执行会话
            // 第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("gghs");

            // 创建消息对象
            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("我渴了！");
            //producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // 通过消息对象提交消息内容
            producer.send(textMessage);

            // 提交会话
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
