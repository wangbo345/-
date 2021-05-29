package com.changgou.dao;

import com.changgou.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/25
 * @description :
 */
public interface SearchMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
