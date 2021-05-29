package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.EsManagerService;
import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/25
 * @description :
 */
@RestController
@RequestMapping("/sku_search")
public class SearchController {
    @Autowired
    private EsManagerService esManagerService;
    @Autowired
    private SearchService searchService;

    /**
     * 全文检索
     * @param paramMap
     * @return
     */
    @GetMapping("/search")
    public Map search(@RequestParam Map<String, String> paramMap) {
        //特殊符号处理
        handlerSearchMap(paramMap);
        Map search = searchService.Search(paramMap);
        return search;
    }
    @GetMapping("/searchByBrand")
    public Map searchByBrand(@RequestParam Map<String, String> paramMap) {
        //特殊符号处理
        handlerSearchMap(paramMap);
        Map search = searchService.searchByBrand(paramMap);
        return search;
    }

    private void handlerSearchMap(Map<String, String> paramMap) {
        if (paramMap != null) {
            Set<Map.Entry<String, String>> entries = paramMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getKey().startsWith("spec_")) {
                    paramMap.put(entry.getKey(), entry.getValue().replace("+", "%2B"));
                }
            }
        }
    }

    /**
     * 创建索引和映射,其实不需要执行,在SkuInfo类被@Document修饰后,会自动创建
     * @return
     */
    @GetMapping("/createIndexAndMapping")
    public Result createIndexAndMapping() {
        esManagerService.createIndexAndMapping();
        return new Result(true, StatusCode.OK, "创建成功");
    }
    /**
     * 导入所有审核通过的库存数据到ES索引库
     * @return
     */
    @GetMapping("/importAll")
    public Result importAllDataToES() {
        esManagerService.importAll();
        return new Result(true, StatusCode.OK, "导入数据成功!");
    }

}
