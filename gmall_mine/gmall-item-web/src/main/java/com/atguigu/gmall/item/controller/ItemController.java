package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuInfoService skuInfoService;

    @Reference
    SpuInfoService spuInfoService;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map){
        //sku信息
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);

        //当前sku的spuId
        String spuId = skuInfo.getSpuId();

        //销售属性值列表
        List<SpuSaleAttr> spuSaleAttrs = spuInfoService.getSpuSaleAttrListCheckBySku(spuId,skuId);

        //动态切换的销售属性值对应的skuId的hash表
        List<SkuInfo> skuSaleAttrValueListBySpu = skuInfoService.getSkuSaleAttrValueListBySpu(spuId);

        //转化成hash表
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (SkuInfo info : skuSaleAttrValueListBySpu) {
            String v = info.getId();

            // 拼接销售属性值的id
            String k = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                if(StringUtils.isNotBlank(k)){
                    k = k + "|";
                }
                k = k + skuSaleAttrValue.getSaleAttrValueId();
            }
            System.out.println(k);
            stringStringHashMap.put(k,v);
        }

        System.out.println(stringStringHashMap);
        map.put("skuJsonBrother", JSON.toJSONString(stringStringHashMap));
        map.put("skuInfo",skuInfo);
        map.put("spuSaleAttrListCheckBySku",spuSaleAttrs);
        return "item";
    }
}
