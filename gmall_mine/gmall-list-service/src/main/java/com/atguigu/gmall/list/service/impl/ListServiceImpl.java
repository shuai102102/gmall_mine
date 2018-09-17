package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.list.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();

        // 封装es的dsl查询字符串
        String dsl = getDslQuery(skuLsParam);
        System.err.println(dsl);

        //执行es的查询对象
        Search build = new Search.Builder(dsl).addIndex("gmall0228").addType("SkuLsInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;
            if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
                // 取出高亮字段
                Map<String, List<String>> highlight = hit.highlight;
                List<String> skuNameList = highlight.get("skuName");
                String skuNameHiglight = skuNameList.get(0);

                // 替换原始字段
                source.setSkuName(skuNameHiglight);
            }
            skuLsInfoList.add(source);
        }

        return skuLsInfoList;
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(String idJoin) {
        List<BaseAttrInfo> baseAttrInfos = skuAttrValueMapper.selectAttrListByValueIds(idJoin);
        return baseAttrInfos;
    }


    private String getDslQuery(SkuLsParam skuLsParam) {
        // 使用dslquery工具封装参数
        // query bool
        // filter term
        // must match
        SearchSourceBuilder ssb = new SearchSourceBuilder();

        // 符合参数(过滤/搜索)
        BoolQueryBuilder bool = new BoolQueryBuilder();

        if(StringUtils.isNotBlank(skuLsParam.getCatalog3Id())){
            TermsQueryBuilder t = new TermsQueryBuilder("catalog3Id", skuLsParam.getCatalog3Id());
            bool.filter(t);
        }
        if(skuLsParam.getValueId()!=null && skuLsParam.getValueId().length>0){
            String[] valueIds = skuLsParam.getValueId();
            for (String valueId : valueIds) {
                TermsQueryBuilder t = new TermsQueryBuilder("skuAttrValueList.valueId", valueId);
                bool.filter(t);
            }
        }

        if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
            MatchQueryBuilder m = new MatchQueryBuilder("skuName", skuLsParam.getKeyword());
            bool.must(m);
        }

        ssb.query(bool);
        ssb.from(0);
        ssb.size(100);

        //设置关键字高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        ssb.highlight(highlightBuilder);

        return ssb.toString();
    }
}
/*
package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.list.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();

        // 封装es的dsl查询字符串
        String dsl = getDslQuery(skuLsParam);
        System.err.println(dsl);
        // 执行es的查询对象
        Search build = new Search.Builder(dsl).addIndex("gmall0228").addType("SkuLsInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析返回结果
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);

        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;
            if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
                // 取出高亮字段
                Map<String, List<String>> highlight = hit.highlight;
                List<String> skuNameList = highlight.get("skuName");
                String skuNameHiglight = skuNameList.get(0);

                // 替换原始字段
                source.setSkuName(skuNameHiglight);
            }
            skuLsInfoList.add(source);
        }

        return skuLsInfoList;

    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(String idJoin) {

        List<BaseAttrInfo> baseAttrInfos = skuAttrValueMapper.selectAttrListByValueIds(idJoin);

        return baseAttrInfos;
    }

    private String getDslQuery(SkuLsParam skuLsParam) {

        // 使用dslquery工具封装参数
        // query bool
        // filter term
        // must match
        SearchSourceBuilder ssb = new SearchSourceBuilder();

        // 符合参数(过滤/搜索)
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // 分类过滤
        if(StringUtils.isNotBlank(skuLsParam.getCatalog3Id())){
            TermsQueryBuilder t = new TermsQueryBuilder("catalog3Id",skuLsParam.getCatalog3Id());
            bool.filter(t);
        }

        // 属性值过滤
        if(skuLsParam.getValueId()!=null&&skuLsParam.getValueId().length>0){
            String[] valueIds = skuLsParam.getValueId();
            for (String valueId : valueIds) {
                TermsQueryBuilder t = new TermsQueryBuilder("skuAttrValueList.valueId",valueId);
                bool.filter(t);
            }
        }


        //关键字搜索
        if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
            MatchQueryBuilder m = new MatchQueryBuilder("skuName",skuLsParam.getKeyword());
            bool.must(m);
        }
        ssb.query(bool);
        ssb.from(0);
        ssb.size(100);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        ssb.highlight(highlightBuilder);

        return ssb.toString();

    }
}
*/
