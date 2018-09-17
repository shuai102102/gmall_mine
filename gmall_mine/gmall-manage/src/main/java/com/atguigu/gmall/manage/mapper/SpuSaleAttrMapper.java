package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr>{
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(HashMap<Object, Object> objectObjectHashMap);
}
