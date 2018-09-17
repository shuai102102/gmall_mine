package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMappr;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMappr;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SkuInfoServiceImpl implements SkuInfoService{

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMappr skuAttrValueMappr;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public SkuInfo getSkuInfo(String skuId){
        String skuInfoJson = "";
        SkuInfo skuInfo = new SkuInfo();
        String key = "sku" +skuId+ ":info";

        // 先访问缓存数据
        Jedis jedis = redisUtil.getJedis();
        if(jedis != null){
            skuInfoJson = jedis.get(key);
        }

        if(jedis == null || StringUtils.isBlank(skuInfoJson)){

            if(!"empty".equals(skuInfoJson)){
                SkuInfo skuInfoFromDb = null;
                System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "开始申请分布式锁");

                String ok = jedis.set("sku:"+skuId+":lock","1","nx","px",10000);
                if("ok".equalsIgnoreCase(ok)){
                    System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "已申请分布式锁成功，开始访问数据库");
                    //访问DB取出要查询的skuInfo
                    skuInfoFromDb = getSkuInfoFromDb(skuId);

                    if(skuInfoFromDb == null){
                        System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "申请分布式锁成功，访问数据库为空，将缓存中商品对象置为空值");
                        jedis.setex(key,60*30,"empty");

                        if(jedis != null){
                            jedis.close();
                        }
                        skuInfo = null;
                    }else {
                        System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "申请分布式锁成功，成功访问数据库，将数据返回给前端");
                        if( jedis != null){
                            //将访问DB获取的skuInfo存入redis中
                            String s = JSON.toJSONString(skuInfoFromDb);
                            jedis.set(key,s);
                            jedis.close();
                        }
                        //并将值返回回去
                        skuInfo = skuInfoFromDb;
                    }

                    //将分布式锁归还
                    System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "归还分布式锁");
                    jedis.del("sku:"+skuId+":lock");
                    return skuInfo;
                }else {
                    System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "未获得分布式锁，分布式锁被占用，等待3秒，开始自旋。。。");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getSkuInfo(skuId);
                }
            }else{
                System.err.println("线程" + Thread.currentThread().getName() + "数据库中没有数据，直接结束访问");
            }
        }else{
            skuInfo = JSON.parseObject(skuInfoJson,SkuInfo.class);
        }

        return skuInfo;
    }


    public SkuInfo getSkuInfoFromDb(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);

        skuInfo1.setSkuImageList(skuImages);

        return skuInfo1;
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {

        // 根据id判断保存或者更新
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);

        } else {
            // 更新
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);

        }

        // 根据id判断保存或者更新
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);

        } else {
            // 更新
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);

        }


        // 保存图片
        Example img = new Example(SkuImage.class);
        img.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuImageMapper.deleteByExample(img);//delete from sku_image where sku_id = skuInfo.getId()
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImage.setId(null);
            skuImageMapper.insertSelective(skuImage);

            System.out.println(1111);
        }

        //保存平台属性
        Example attrValue = new Example(SkuAttrValue.class);
        attrValue.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuAttrValueMappr.deleteByExample(attrValue);//delete from sku_image where sku_id = skuInfo.getId()
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValue.setId(null);
            skuAttrValueMappr.insertSelective(skuAttrValue);
        }

        //保存 销售属性
        Example saleAttrVallue = new Example(SkuSaleAttrValue.class);
        saleAttrVallue.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(saleAttrVallue);//delete from sku_image where sku_id = skuInfo.getId()
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setId(null);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId){

        List<SkuInfo>  skuInfos = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuInfos;
    }

    @Override
    public List<SkuInfo> getSkuInfoByCatalog3Id(int i) {

        String catalog3Id = i + "";
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);

        for (SkuInfo info : select) {
            String skuId = info.getId();
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> select1 = skuAttrValueMappr.select(skuAttrValue);
            info.setSkuAttrValueList(select1);
        }

        return select;
    }

    @Override
    public boolean checkSkuPrice(CartInfo cartInfo) {
        boolean b = false;

        BigDecimal skuPrice = cartInfo.getSkuPrice();

        String skuId = cartInfo.getSkuId();

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);

        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);

        if(skuInfo1!=null){
            BigDecimal price =  skuInfo1.getPrice();
            int i = skuPrice.compareTo(price);
            if(i == 0){
               b= true;
           }
        }
        return b;
    }
}
