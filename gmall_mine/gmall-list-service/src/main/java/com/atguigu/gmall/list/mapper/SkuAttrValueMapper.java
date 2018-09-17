package com.atguigu.gmall.list.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuAttrValueMapper extends Mapper<SkuAttrValue>{
    List<BaseAttrInfo> selectAttrListByValueIds(@Param("idJoin")String idJoin);
}
