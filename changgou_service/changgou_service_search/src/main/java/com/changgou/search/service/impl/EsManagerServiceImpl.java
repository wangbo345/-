package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.dao.SearchMapper;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.service.EsManagerService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/25
 * @description :
 */
@Service
public class EsManagerServiceImpl implements EsManagerService {
    @Autowired
    private SearchMapper searchMapper;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    /**
     * 创建索引库
     */
    @Override
    public void createIndexAndMapping() {
        //创建索引
        elasticsearchTemplate.createIndex(SkuInfo.class);
        //创建映射
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    /**
     * 根据spu_id数据导入sku列表到索引库
     * @param spuId 商品id
     */
    @Override
    public void importDataToESBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfos);
    }

    /**
     * 导入全部数据到索引库
     */
    @Override
    public void importAll() {
        //先查询所有已上架商品,status等于1代表已上架
        //先构建查询条件
        Map<String, Object> map = new HashMap<>();
        map.put("status", "1");
        Result list = skuFeign.findList(map);
        //将查询出来的数据进行JSON转换
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(list.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setPrice(skuInfo.getPrice());
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfos);
    }

    /**
     * 根据spuId删除索引库中的索引
     * @param id
     */
    @Override
    public void deleteSpuFromES(String id) {
        //先查询spuId下的skuList
        List<Sku> skuListBySpuId = skuFeign.findSkuListBySpuId(id);
        //删除索引
        for (Sku sku : skuListBySpuId) {
            searchMapper.deleteById(Long.valueOf(sku.getId()));
        }
    }
}
