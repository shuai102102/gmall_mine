package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;

import java.util.List;

public interface AttrService {
    List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrListByAttrId(String attrId);

    void delAttrBtAttrId(String attrId);
}
