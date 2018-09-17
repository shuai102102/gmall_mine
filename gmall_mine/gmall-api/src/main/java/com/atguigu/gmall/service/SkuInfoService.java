package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

public interface SkuInfoService {

    SkuInfo getSkuInfo(String skuId);

    void saveSku(SkuInfo skuInfo);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getSkuInfoByCatalog3Id(int i);

    boolean checkSkuPrice(CartInfo cartInfo);
}
