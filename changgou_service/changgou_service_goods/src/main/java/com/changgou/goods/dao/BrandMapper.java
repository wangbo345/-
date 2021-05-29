package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
@Repository
public interface BrandMapper extends Mapper<Brand> {
    @Select("SELECT * FROM tb_brand WHERE id in (SELECT brand_id FROM tb_category_brand WHERE category_id in (SELECT id FROM tb_category WHERE `name`=#{cateName}))")
    List<Brand> findBrandByCateName(@Param("cateName") String cateName);
}
