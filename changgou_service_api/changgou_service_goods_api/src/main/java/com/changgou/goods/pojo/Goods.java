package com.changgou.goods.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * @author : wangbo
 * @version : 1.0
 * @date :Create in 2021/5/12
 * @description :
 */
public class Goods implements Serializable {
    private Spu spu;
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
