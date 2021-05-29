package com.changgou.search.service;

import java.util.Map;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/27
 * @description :
 */
public interface SearchService {
    /**
     * 全文检索
     * @param paramMap
     * @return
     */
    public Map Search(Map<String,String> paramMap) ;
    public Map searchByBrand(Map<String, String> paramMap);
}
