package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;


@Controller
public class ListController {

    @Reference
    ListService listService;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap modelMap){

       String catalog3Id = skuLsParam.getCatalog3Id();

        List<SkuLsInfo> search = listService.search(skuLsParam);

        String[] valueIds = getValueIds(search);
        String idJoin = StringUtils.join(valueIds, ",");
        System.out.println(idJoin);
        List<BaseAttrInfo> baseAttrInfos = listService.getAttrListByValueIds(idJoin);

        // 制作面包屑列表
        List<Crumb> crumbs = new ArrayList<>();

        // 根据备选中的属性值id删除该属性值所在的属性
        String[] requestValueIds = skuLsParam.getValueId();
        if(requestValueIds != null && requestValueIds.length > 0){
            Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
            while(iterator.hasNext()){
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    String valueId = baseAttrValue.getId();
                    for (String requestValueId : requestValueIds) {
                        if (requestValueId.equals(valueId)){
                            //制作面包屑
                            String urlParam = getUrlParam(skuLsParam,requestValueId);
                            Crumb crumb = new Crumb();
                            crumb.setValueName(baseAttrValue.getValueName());
                            crumb.setUrlParam(urlParam);
                            crumbs.add(crumb);

                            // 重属性列表中，将该属性值所属的属性删除
                            iterator.remove();
                        }
                    }
                }
            }
        }

        if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
            modelMap.put("keyword",skuLsParam.getKeyword());
        }

        //当前请求字符串
        String urlParam = getUrlParam(skuLsParam);
        modelMap.put("skuLsInfoList",search);//sku列表
        modelMap.put("attrList",baseAttrInfos);//平台属性列表
        modelMap.put("urlParam",urlParam);//根据参数拼接请求字符串
        modelMap.put("attrValueSelectedList",crumbs);//面包屑
        return "list";
    }

    private String getUrlParam(SkuLsParam skuLsParam, String... requestValueIds) {
        String crumbValueId = "";

        if(requestValueIds != null && requestValueIds.length>0){
            crumbValueId = requestValueIds[0];
        }

        String urlParam = "";
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueIds = skuLsParam.getValueId();

        if(StringUtils.isNotBlank(catalog3Id)){
            urlParam = urlParam + "catalog3Id=" +catalog3Id;
        }

        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(catalog3Id)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword="+keyword;
        }

        if(valueIds != null && valueIds.length>0){
            for(int i = 0;i < valueIds.length; i++){
                // 请求字符串中不包含需要排除的value属性值id
                if(StringUtils.isBlank(crumbValueId)||!valueIds[i].equals(crumbValueId)){
                    urlParam = urlParam + "&valueId="+valueIds[i];
                }
            }
        }
        return urlParam;
    }


    private String[] getValueIds(List<SkuLsInfo> search) {
        Set<String> set = new HashSet<>();

        for (SkuLsInfo skuLsInfo : search) {
           List<SkuLsAttrValue> skuLsInfoSkuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuLsInfoSkuAttrValueList) {
                set.add(skuLsAttrValue.getValueId());
            }
        }
        String[] valueIds = new String[set.size()];
        Iterator<String> iterator = set.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String next = iterator.next();
            valueIds[i++] = next;
        }

        return valueIds;
    }
}
/*package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap map) {
        String catalog3Id = skuLsParam.getCatalog3Id();

        List<SkuLsInfo> search = listService.search(skuLsParam);

        // 根据检索结果获得所有去重的valueId
        String[] valueIds = getValueIds(search);
        String idJoin = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> baseAttrInfos = listService.getAttrListByValueIds(idJoin);

        // 制作面包屑列表
        List<Crumb> crumbs = new ArrayList<>();

        // 根据备选中的属性值id删除该属性值所在的属性
        String[] requestValueIds = skuLsParam.getValueId();
        if (requestValueIds != null && requestValueIds.length > 0) {
            Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
            while (iterator.hasNext()) {
                BaseAttrInfo baseAttrInfo = iterator.next();

                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    // 属性列表中的id
                    String valueId = baseAttrValue.getId();

                    for (String requestValueId : requestValueIds) {
                        if (valueId.equals(requestValueId)) {
                            // 制作面包屑列表
                            String urlParam = getUrlParam(skuLsParam, requestValueId);
                            Crumb crumb = new Crumb();
                            String valueName = baseAttrValue.getValueName();
                            crumb.setUrlParam(urlParam);
                            crumb.setValueName(valueName);
                            crumbs.add(crumb);

                            // 重属性列表中，将该属性值所属的属性删除
                            iterator.remove();
                        }
                    }
                }
            }
        }

        // 制作面包屑
//        String[] crumbsValueIds = skuLsParam.getValueId();
//        for (String crumbsValueId : crumbsValueIds) {
//            // 制作面包屑列表
//            String urlParam = getCrumbUrlParam(skuLsParam, crumbsValueId);
//            Crumb crumb = new Crumb();
//            String valueName = "";//单独调用方法根据属性值id获得属性值名称
//            crumb.setUrlParam(urlParam);
//            crumb.setValueName(valueName);
//            crumbs.add(crumb);
//        }


        // 当前的查询的关键字
        if (StringUtils.isNotBlank(skuLsParam.getKeyword())) {
            map.put("keyword", skuLsParam.getKeyword());
        }
        //  当前请求参数字符串
        String urlParam = getUrlParam(skuLsParam);
        map.put("urlParam", urlParam);
        map.put("attrList", baseAttrInfos);
        map.put("skuLsInfoList", search);
        map.put("attrValueSelectedList", crumbs);

        return "list";
    }

    *//***
     * 根据参数拼接请求字符串
     * @param skuLsParam
     * @return
     *//*
    private String getUrlParam(SkuLsParam skuLsParam, String... requestValueIds) {

        String crumbValueId = "";
        if(requestValueIds!=null&&requestValueIds.length>0){
            crumbValueId = requestValueIds[0];
        }

        String urlParam = "";
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueId = skuLsParam.getValueId();

        if (StringUtils.isNotBlank(catalog3Id)) {
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }


        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(catalog3Id)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }




        if (valueId != null && valueId.length > 0) {
            for (int i = 0; i < valueId.length; i++) {

                // 请求字符串中不包含需要排除的value属性值id
                if(StringUtils.isBlank(crumbValueId)||!valueId[i].equals(crumbValueId)){
                    urlParam = urlParam + "&" + "valueId=" + valueId[i];
                }
            }
        }



        return urlParam;
    }

    *//***
     * 根据结果查询包含的平台属性列表
     * @param search
     * @return
     *//*
    private String[] getValueIds(List<SkuLsInfo> search) {
        Set<String> set = new HashSet<>();

        for (SkuLsInfo skuLsInfo : search) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                set.add(skuLsAttrValue.getValueId());
            }
        }

        String[] valueIds = new String[set.size()];
        Iterator<String> iterator = set.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String next = iterator.next();
            valueIds[i] = next;
            i++;
        }
        return valueIds;
    }
}*/
