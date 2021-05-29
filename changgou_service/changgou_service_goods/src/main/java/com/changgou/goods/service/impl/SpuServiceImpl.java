package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.utils.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mysql.cj.xdevapi.JsonParser;
import com.netflix.discovery.converters.Auto;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryBrandMapper categoryBrandMapper;
    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;
    /**
     * 添加商品对象
     * @param goods
     */
    @Override
    public void addGoods(Goods goods) {
        //先从前端json数据转换而成的实体类中提取出Spu,Sku
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();
        //再分别将Spu,Sku存储到他们自己的数据库表中
        //a.存储Sku
        long spuId = idWorker.nextId();
        spu.setId(String.valueOf(spuId));
        int i = spuMapper.insertSelective(spu);
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        /**
         * 添加分类与品牌之间的关联
         */
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        //判断是否有这个品牌和分类的关系数据
        if(count == 0) {
            //如果没有关系数据则添加品牌和分类关系数据
            categoryBrandMapper.insert(categoryBrand);
        }
        //b.存储Spu
        if (skuList!=null&&skuList.size()>0){
            for (Sku sku : skuList) {
                //设置id
                sku.setId(String.valueOf(idWorker.nextId()));
                //设置Sku名称
                String name = sku.getName();
                String spec = sku.getSpec();
                if (StringUtils.isEmpty(spec)) {
                    sku.setSpec("{}");
                }
                Map<String, String> map = JSON.parseObject(sku.getSpec(), Map.class);
                if (map!=null&&map.size()>0){
                    for (String value : map.values()) {
                        name+=" "+value;
                    }
                }
                sku.setName(name);
                //设置创建日期
                sku.setCreateTime(new Date());
                //设置修改日期
                sku.setUpdateTime(new Date());
                //设置商品分类id
                sku.setCategoryId(category.getId());
                sku.setCategoryName(category.getName());
                sku.setBrandName(brand.getName());
                sku.setSpuId(String.valueOf(spuId));
                int i1 = skuMapper.insertSelective(sku);

            }
        }
    }

    /**
     * 根据spu_id查询商品信息
     * @param spuId
     */
    @Override
    public Goods selectGoodsBySpuId(String spuId) {
        Goods goods=new Goods();
        //添加spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        goods.setSpu(spu);
        //添加sku列表
        Example example=new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spuId);
        List<Sku> skus = skuMapper.selectByExample(example);
        goods.setSkuList(skus);
        return goods;
    }

    /**
     * 更新商品信息
     * @param goods
     */
    @Override
    @Transactional
    public void updateGoods(Goods goods) {
        //修改spu
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKey(spu);
        //修改sku,先删除原有的skuList,再添加即可
        List<Sku> skuList = goods.getSkuList();
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        skuMapper.deleteByExample(example);
        //再分别将Spu,Sku存储到他们自己的数据库表中
        //a.存储Sku
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //b.存储Spu
        if (skuList != null && skuList.size() > 0) {
            for (Sku sku : skuList) {
                //设置id
                sku.setId(String.valueOf(idWorker.nextId()));
                //设置Sku名称
                String name = sku.getName();
                String spec = sku.getSpec();
                if (StringUtils.isEmpty(spec)) {
                    sku.setSpec("{}");
                }
                Map<String, String> map = JSON.parseObject(sku.getSpec(), Map.class);
                if (map != null && map.size() > 0) {
                    for (String value : map.values()) {
                        name += " " + value;
                    }
                }
                sku.setName(name);
                //设置创建日期
                sku.setCreateTime(new Date());
                //设置修改日期
                sku.setUpdateTime(new Date());
                //设置商品分类id
                sku.setCategoryId(category.getId());
                sku.setCategoryName(category.getName());
                sku.setBrandName(brand.getName());
                sku.setSpuId(String.valueOf(spu.getId()));
                int i1 = skuMapper.insertSelective(sku);
            }
        }
    }

    /**
     * 商品审核
     * @param id
     */
    @Override
    public void auditGoods(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setStatus("1");//已审核
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 商品下架
     * @param id
     */
    @Override
    public void pull(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
        //将商品下架的消息发送到RabbitMQ指定的交换机,RabbitMq的交换机在转发给指定的队列
        rabbitMessagingTemplate.convertAndSend("goods_down_exchange","",id);
    }

    /**
     *商品上架
     * @param id
     */
    @Override
    public void put(String id) {
        /**
         * 更改数据库中上架状态
         */
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if ("0".equals(spu.getStatus())){
            throw new RuntimeException("未通过审核的商品不能上架");
        }
        spu.setIsMarketable("1");//上架状态
        spuMapper.updateByPrimaryKeySelective(spu);
        //将消息发送给RabbitMQ
        rabbitMessagingTemplate.convertAndSend("goods_up_exchange","",id);
    }

    /**
     * 逻辑删除
     * @param id
     */
    @Override
    public void deleteLogic(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if ("1".equals(spu.getIsMarketable())) {
            throw new RuntimeException("已经上架的商品不能删除");
        }
        spu.setIsDelete("1");
        spu.setStatus("0");//逻辑删除的商品想重新上架必须再次审核才行
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 恢复逻辑删除的数据
     * @param id
     */
    @Override
    public void restore(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if ("0".equals(spu.getIsDelete())) {
            throw new RuntimeException("该商品未被删除");
        }
        spu.setIsDelete("0");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 物理删除
     * @param id
     */
    @Override
    public void realDelete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if ("0".equals(spu.getIsDelete())) {
            throw new RuntimeException("此商品未逻辑删除,请先执行逻辑删除!");
        }
        spuMapper.deleteByPrimaryKey(spu.getId());
    }

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }


    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        spuMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }


    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
