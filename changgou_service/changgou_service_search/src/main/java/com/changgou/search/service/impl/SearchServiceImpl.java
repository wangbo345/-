package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.pojo.Sku;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/27
 * @description :
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    //设置每页查询的条数
    public final static Integer PAGE_SIZE=20;

    /**
     * 根据分词后的关键词进行查询
     * @param paramMap
     * @return
     */
    @Override
    public Map Search(Map<String, String> paramMap) {
        //返回结果的map
        Map<String, Object> resultMap = new HashMap<>();
        //有条件才能查询
        if (paramMap != null) {
            //组合条件对象
            BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
            //关键词,假设前方传来关键词的变量是keyWords
            if (!StringUtils.isEmpty(paramMap.get("keyWords"))) {
                //根据关键词查询,切分词后查询
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", paramMap.get("keyWords")).operator(Operator.AND));
            }
            //原生搜索实现类
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
            //执行查询返回结果对象
            AggregatedPage<SkuInfo> aggregatedPage=elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> list=new ArrayList<>();
                    SearchHits hits = searchResponse.getHits();
                    if (hits != null) {
                        for (SearchHit hit : hits) {
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            list.add((T) skuInfo);
                        }
                    }

                    return new AggregatedPageImpl<T>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
                }
            });
            //总条数
            resultMap.put("total", aggregatedPage.getTotalElements());
            //总页数
            resultMap.put("totalPages", aggregatedPage.getTotalPages());
            //查询结果集合
            resultMap.put("rows", aggregatedPage.getContent());
            return resultMap;
        }

        return null;
    }

    @Override
    public Map searchByBrand(Map<String, String> paramMap) {
        //返回结果的map
        Map<String, Object> resultMap = new HashMap<>();
        //有条件才能查询
        if (paramMap != null) {
            //组合条件对象
            BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
            //关键词,假设前方传来关键词的变量是keyWords
            if (!StringUtils.isEmpty(paramMap.get("keyWords"))) {
                //根据关键词查询,切分词后查询
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", paramMap.get("keyWords")).operator(Operator.AND));
            }
            //条件:品牌
            if (!StringUtils.isEmpty(paramMap.get("brand"))) {
                //根据关键词查询,切分词后查询
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", paramMap.get("brand")));
            }
            //条件 规格
            for (String key : paramMap.keySet()) {
                if (key.startsWith("spec_")) {
                    String value = paramMap.get(key).replace("%2B", "+");
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword",value));
                }
            }
            //3:条件 价格
            if (!StringUtils.isEmpty(paramMap.get("price"))) {
                String[] p = paramMap.get("price").split("-");
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(p[0]));
                if (p.length == 2) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(p[1]));
                }
            }

            //原生搜索实现类
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
            //5:高亮
            HighlightBuilder.Field field = new HighlightBuilder
                    .Field("name")
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");
            nativeSearchQueryBuilder.withHighlightFields(field);
            //品牌搜索,聚合查询
            String skuBrand = "skuBrand";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));
            //7. 规格聚合(分组)查询
            String skuSpec = "skuSpec";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));
            //8: 排序
            if (!StringUtils.isEmpty(paramMap.get("sortField"))) {
                if ("ASC".equals(paramMap.get("sortRule"))) {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(paramMap.get("sortField")).order(SortOrder.ASC));
                } else {

                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(paramMap.get("sortField")).order(SortOrder.DESC));
                }

            }

            //分页
            String pageNum = paramMap.get("pageNum");
            if (null == pageNum) {
                pageNum = "1";
            }
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, PAGE_SIZE ));
            //执行查询返回结果对象
            AggregatedPage<SkuInfo> aggregatedPage=elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> list=new ArrayList<>();
                    SearchHits hits = searchResponse.getHits();
                    if (hits != null) {
                        for (SearchHit hit : hits) {
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                            if (null != highlightFields && highlightFields.size() > 0) {
                                skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                            }
                            list.add((T) skuInfo);
                        }
                    }

                    return new AggregatedPageImpl<T>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
                }
            });
            //总条数
            resultMap.put("total", aggregatedPage.getTotalElements());
            //总页数
            resultMap.put("totalPages", aggregatedPage.getTotalPages());
            //查询结果集合
            resultMap.put("rows", aggregatedPage.getContent());
            //获取品牌聚合结果
            StringTerms brandTerms=(StringTerms) aggregatedPage.getAggregation(skuBrand);
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList", brandList);
            //15. 获取规格聚合结果
            StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList", specList(specList));
            //16. 返回当前页
            resultMap.put("pageNum", pageNum);
            return resultMap;
        }

        return null;
    }

    //处理规格集合
    public Map<String, Set<String>> specList(List<String> specList) {

        Map<String, Set<String>> specMap = new HashMap<>();

        if (null != specList && specList.size() > 0) {

            for (String spec : specList) {

                Map<String, String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    Set<String> specValues = specMap.get(key);
                    if (null == specValues) {
                        specValues = new HashSet<>();
                    }
                    specValues.add(value);
                    specMap.put(key, specValues);
                }
            }
        }
        return specMap;
    }


}
