package com.atguigu.gmall.manage.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class AttrController {

    @Reference
    AttrService attrService;

    @RequestMapping("delAttrBtAttrId")
    @ResponseBody
    public String delAttrBtAttrId(@RequestParam Map<String,String> map){
        String attrId = map.get("attrId");
        attrService.delAttrBtAttrId(attrId);
        return "success";
    }

    @RequestMapping("getAttrListByAttrId")
    @ResponseBody
    public List<BaseAttrValue> getAttrListByAttrId(@RequestParam Map<String,String> map){
        String attrId = map.get("attrId");
        List<BaseAttrValue> attrListByAttrId = attrService.getAttrListByAttrId(attrId);
        return attrListByAttrId;
    }

    @RequestMapping("saveAttr")
    @ResponseBody
    public String saveAttr(BaseAttrInfo baseAttrInfo){

        attrService.saveAttr(baseAttrInfo);
        return "success";
    }

    @RequestMapping("getAttrListByCtg3")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListByCtg3(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        List<BaseAttrInfo> attrListByCtg3 = attrService.getAttrListByCtg3(catalog3Id);
        return attrListByCtg3;
    }
}
