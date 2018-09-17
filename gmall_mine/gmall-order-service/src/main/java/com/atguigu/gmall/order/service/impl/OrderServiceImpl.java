package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String genTradeCode(String userId) {
        String tradeCode = "atguigu" + UUID.randomUUID().toString();

        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + userId +":tradeCode",60*10,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean b = false;
        Jedis jedis = redisUtil.getJedis();

        String tradeCodeRedis = jedis.get("user:"+userId+":tradeCode");
        if (StringUtils.isNotBlank(tradeCodeRedis)){
            if (tradeCodeRedis.equals(tradeCode)){
                b=true;
                jedis.del("user:" + userId + ":tradeCode");
            }

        }
        jedis.close();
        return b;
    }

    @Override
    public void saveOrder (OrderInfo orderInfo){
        //保存订单信息
        orderInfoMapper.insertSelective(orderInfo);

        //根据订单主键保存，订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }

    }

    @Override
    public OrderInfo getPaymentByOutTradeNo(String orderId) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(orderId);

        OrderInfo orderInfo1 = orderInfoMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo1.getId());
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);
        orderInfo1.setOrderDetailList(select);

        return orderInfo1;
    }

    @Override
    public  void updateOrder(OrderInfo orderInfo){

        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",orderInfo.getOutTradeNo());
        orderInfoMapper.updateByExampleSelective(orderInfo,example);
    }

    @Override
    public void sentOrderResult(String out_trade_no) {
        Connection connection = activeMQUtil.getConnection();

        try {
            //获得连接MQ
            connection.start();

            //创建会话
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testQueue = session.createQueue("ORDER_RESULT_QUEUE");

            //创建消息对象
            MessageProducer producer = session.createProducer(testQueue);
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(out_trade_no);

//            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            //通过消息对象提交消息内容
            producer.send(textMessage);

            //提交会话
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
