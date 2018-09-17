package com.atguigu.gmall.manage.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.util.MyFdfsUploadUtil;
import com.atguigu.gmall.service.SpuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class SpuController {


    @Reference
    SpuInfoService spuInfoService;


    @RequestMapping("getSpuImage")
    @ResponseBody
    public List<SpuImage> getSpuImage(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");
        List<SpuImage> spuImages = spuInfoService.getSpuImage(spuId);
        return spuImages;
    }

    @RequestMapping("getSpuSaleAttrGroup")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrGroup(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");
        List<SpuSaleAttr> spuSaleAttrs = spuInfoService.getSpuSaleAttrGroup(spuId);
        return spuSaleAttrs;
    }

    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){

        spuInfoService.saveSpu(spuInfo);
        return "success";
    }

    @RequestMapping("uploadSpuImg")
    @ResponseBody
    public String uploadSpuImg(@RequestParam("file")MultipartFile file){

        return MyFdfsUploadUtil.uploadImage(file);
//        return "http://192.168.186.102/group1/M00/00/00/wKi6ZltIfbCAVlvdAAJXwtzdKF4967.jpg";
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        return spuInfoService.baseSaleAttrList();
    }

    @RequestMapping("getSpuInfoListByCtg3")
    @ResponseBody
    public List<SpuInfo> getSpuInfoListByCtg3(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        List<SpuInfo> spuInfos =  spuInfoService.getSpuInfoListByCtg3(catalog3Id);
        return spuInfos;
    }
}
