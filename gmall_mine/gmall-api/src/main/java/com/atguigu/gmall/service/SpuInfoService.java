package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SpuInfoService {

    List<SpuInfo> getSpuInfoListByCtg3(String catalog3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId);

    List<SpuSaleAttr> getSpuSaleAttrGroup(String spuId);

    List<SpuImage> getSpuImage(String spuId);
}
