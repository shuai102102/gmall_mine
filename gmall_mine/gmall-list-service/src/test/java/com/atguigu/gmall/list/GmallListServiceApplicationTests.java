package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
//import com.atguigu.gmall.list.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Reference
	SkuInfoService skuInfoService;

	@Test
	public void getData()throws Exception{
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.toString();

		//封装es的dsl的查询字符串
		String dsl = "{\n" +
				"  \"query\": {\n" +
				"    \"bool\": {\n" +
				"      \"filter\": [\n" +
				"        {\"term\":{\"skuAttrValueList.valueId\":\"41\"}},\n" +
				"        {\"term\":{\"skuAttrValueList.valueId\":\"45\"}}\n" +
				"        ]\n" +
				"      ,\n" +
				"      \"must\": [\n" +
				"        {\n" +
				"          \"match\": {\n" +
				"            \"skuName\": \"小米\"\n" +
				"          }\n" +
				"        }\n" +
				"      ]\n" +
				"    }\n" +
				"  }\n" +
				"  ,\n" +
				"  \"size\": 50\n" +
				"  , \"from\": 0\n" +
				"}";
		//执行es的查询对象
		Search build = new Search.Builder(dsl).addIndex("gmall0228").addType("SkuLsInfo").build();
		SearchResult execute = jestClient.execute(build);

		//解析返回的结果
		List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
		List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
		for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
			SkuLsInfo source = hit.source;
			skuLsInfoList.add(source);
		}

		System.out.println(skuLsInfoList);
	}

	@Test
	public void contextLoads() throws Exception {
		//查询skuLsInfo的数据
		List<SkuInfo> skuInfoList = skuInfoService.getSkuInfoByCatalog3Id(61);

		//封装skuLsInfo的数据
		List<SkuLsInfo> skuLsInfos = new ArrayList<>();
		for (SkuInfo skuInfo : skuInfoList) {
			SkuLsInfo skuLsInfo = new SkuLsInfo();
			BeanUtils.copyProperties(skuLsInfo,skuInfo);
			skuLsInfos.add(skuLsInfo);
		}

		// 将skuLsInfo的数据导入到es中
		for (SkuLsInfo skuLsInfo : skuLsInfos) {
			Index build = new Index.Builder(skuLsInfo).index("gmall0228").type("SkuLsInfo").id(skuLsInfo.getId()).build();
			jestClient.execute(build);
		}
	}

}
