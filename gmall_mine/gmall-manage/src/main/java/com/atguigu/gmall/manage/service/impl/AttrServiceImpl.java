package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id) {
        BaseAttrInfo b = new BaseAttrInfo();
        b.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.select(b);

        // 放入属性值列表
        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
            String id = baseAttrInfo.getId();
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(id);
            List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.select(baseAttrValue);

            baseAttrInfo.setAttrValueList(baseAttrValues);
        }

        return baseAttrInfos;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        String id = baseAttrInfo.getId();
        if (StringUtils.isBlank(id)){
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }else{
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.updateByPrimaryKey(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrListByAttrId(String attrId) {

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public void delAttrBtAttrId(String attrId) {

        baseAttrInfoMapper.deleteByPrimaryKey(attrId);

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        baseAttrValueMapper.delete(baseAttrValue);
    }
}
