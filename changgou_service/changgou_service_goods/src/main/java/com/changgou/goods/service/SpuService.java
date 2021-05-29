package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface SpuService {

    /***
     * 查询所有
     * @return
     */
    List<Spu> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Spu findById(String id);

    /***
     * 新增
     * @param spu
     */
    void add(Spu spu);

    /***
     * 修改
     * @param spu
     */
    void update(Spu spu);

    /***
     * 删除
     * @param id
     */
    void delete(String id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Spu> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(Map<String, Object> searchMap, int page, int size);

    /**
     * 添加商品
     * @param goods
     */
    void addGoods(Goods goods);

    /**
     * 根据spu_id查询商品信息
     * @param spuId
     * @return
     */
    Goods selectGoodsBySpuId(String spuId);

    /**
     * 修改商品信息
     *
     * @param goods
     */
    void updateGoods(Goods goods);

    /**
     * 商品审核
     * @param id
     */
    void auditGoods(String id);

    /**
     * 商品下架
     *
     * @param id
     */
    void pull(String id);

    /**
     * 商品上架
     * @param id
     */
    void put(String id);

    /**
     * 逻辑删除
     * @param id
     */
    void deleteLogic(String id);

    /**
     * 恢复逻辑删除的数据
     * @param id
     */
    void restore(String id);

    void realDelete(String id);
}
