package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class cartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo ifCartExist(String userId, String skuId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfo1 = cartInfoMapper.selectOne(cartInfo);

        return cartInfo1;
    }

    @Override
    public void addCart(CartInfo cartInfo) {
        String id = cartInfo.getId();
        if(StringUtils.isBlank(id)){
            //添加
            cartInfoMapper.insert(cartInfo);
        }else{
            //修改
            Example example = new Example(CartInfo.class);
            //and
            example.createCriteria().andEqualTo("userId",cartInfo.getUserId())
                    .andEqualTo("skuId",cartInfo.getSkuId());
            cartInfoMapper.updateByExampleSelective(cartInfo,example);
        }
    }

    @Override
    public List<CartInfo> cartCache(String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> select = cartInfoMapper.select(cartInfo);

       HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (CartInfo info : select) {
            stringStringHashMap.put(info.getSkuId(), JSON.toJSONString(info));
        }
        Jedis jedis = redisUtil.getJedis();
        //jedis.del("user:"+userId+":cart"); skuId : cartInfoJSON
        jedis.hmset("user:"+userId+":cart",stringStringHashMap);

        jedis.close();

        return select;
    }



    @Override
    public List<CartInfo> getCartListCacheByUser(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        //jedis.del("user:"+userId+":cart"); skuId : cartInfoJSON
        String key = "user:"+userId+":cart";
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals(key);
        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            cartInfos.add(cartInfo);
        }

        return cartInfos;
    }

    @Override
    public void updateCartChecked(CartInfo cartInfo) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",cartInfo.getUserId()).andEqualTo("skuId",cartInfo.getSkuId());

        cartInfoMapper.updateByExampleSelective(cartInfo,example);
    }

    @Override
    public void mergeCart(String userId, List<CartInfo> cartListCookie) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartListDb = cartInfoMapper.select(cartInfo);

        if(cartListDb != null){
            for (CartInfo cartInfoCookie : cartListCookie) {
                //判断此购物车是否在数据库中存在
                boolean b = ifNewCart(cartListDb, cartInfoCookie);
                if (b) {
                    //新车，添加
                    cartInfoCookie.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoCookie);
                    cartListDb.add(cartInfoCookie);
                }else {
                    //老车，更新
                    for (CartInfo cartInfoDb : cartListDb) {
                        if(cartInfoCookie.getSkuId().equals(cartInfoDb.getSkuId())){
                            cartInfoDb.setSkuNum(cartInfoDb.getSkuNum()+cartInfoCookie.getSkuNum());
                            cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                        }
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);
                    }
                }
            }
        }
        //同步购物车缓存
        cartCache(userId);
    }

    @Override
    public List<CartInfo> getCartListCheckedCacheByUser(String userId) {
        List<CartInfo> cartList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        List<String> hvals = jedis.hvals("user:" + userId + ":cart");

        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            if("1".equals(cartInfo.getIsChecked())){
                cartList.add(cartInfo);
            }
        }

        jedis.close();
        return cartList;
    }

    @Override
    public void cleanCart(List<String> cartIds, String userId) {

        //根据被选择的购物车id删除购物车
        String join = StringUtils.join(cartIds, ",");
        cartInfoMapper.deleteCheckedCart(join,userId);

        //重新刷新购物车缓存
        cartCache(userId);
    }

    /***
     * 判断是否是新车
     * @param cartInfos
     * @param cartInfo
     * @return
     */
    private boolean ifNewCart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        if(cartInfos==null){
            return true;
        }
        for (CartInfo info : cartInfos) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                b = false;
                break;
            }
        }
        return b;
    }
}
