package com.changgou.search.controller;

import com.changgou.pojo.SkuInfo;
import com.changgou.search.model.Page;
import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/27
 * @description :
 */
@Controller
@RequestMapping("/search")
public class SearchPageController {

    @Autowired
    private SearchService searchService;

    //对搜索入参带有特殊符号进行处理
    public void handlerSearchMap(Map<String,String> searchMap){

        if(null != searchMap){
            Set<Map.Entry<String, String>> entries = searchMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if(entry.getKey().startsWith("spec_")){
                    searchMap.put(entry.getKey(),entry.getValue().replace("+","%2B"));
                }
            }
        }

    }

    //搜索页面   http://localhost:9009/search/list?keywords=手机&brand=三星&spec_颜色=粉色&
    //入参：Map
    //返回值 Map
    //由于页面是thymeleaf 完成的 属于服务器内页面渲染 跳转页面
    @GetMapping("/list")
    public String search(@RequestParam Map<String, String> searchMap, Model model) throws Exception {

        //特殊符号处理
        handlerSearchMap(searchMap);

        //执行查询返回值
        Map<String, Object> resultMap = searchService.searchByBrand(searchMap);


        model.addAttribute("searchMap", searchMap);
        model.addAttribute("result", resultMap);
        Page<SkuInfo> page = new Page<SkuInfo>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNum").toString()),
                Page.pageSize);
        model.addAttribute("page", page);

        StringBuilder url = new StringBuilder();


        url.append("/search/list");
        if (null != searchMap && searchMap.size() > 0) {
            url.append("?");
            Set<Map.Entry<String, String>> entries = searchMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (!"pageNum".equals(entry.getKey()) && !"sortRule".equals(entry.getKey())
                        && !"sortField".equals(entry.getKey())) {
                    url.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        model.addAttribute("url", url.toString());
        System.out.println(url.toString());


        return "search";
    }


}
