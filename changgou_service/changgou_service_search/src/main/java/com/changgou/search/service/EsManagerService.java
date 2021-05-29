package com.changgou.search.service;

import org.springframework.stereotype.Repository;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/25
 * @description :
 */
@Repository
public interface EsManagerService {
    /**
     * 创建索引库结构
     */
    public void createIndexAndMapping();

    /**
     * 根据spuid导入数据到ES索引库
     * @param spuId 商品id
     */
    public void importDataToESBySpuId(String spuId);

    /**
     * 导入全部数据到ES索引库
     */
    public void importAll();

    /**
     * 根据spuId删除索引库中的索引
     * @param id
     */
    void deleteSpuFromES(String id);
}
