package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/25
 * @description :
 */
@FeignClient(name="goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /***
     * 多条件搜索商品数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap);

    /**
     * 根据spuId查询商品列表
     * @param spuId
     * @return
     */
    @GetMapping(value = "/findSkuListBySpuId/{spuId}")
    public List<Sku> findSkuListBySpuId(@PathVariable String spuId);

}
