package com.changgou.goods.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.service.SpuService;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;
    /***
     * 新增数据
     * @param goods
     * @return
     */
    @PostMapping("/addGoods")
    public Result addGoods(@RequestBody Goods goods){
        spuService.addGoods(goods);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /**
     * 根据spu_id查询商品信息
     * @param spuId
     * @return
     */
    @GetMapping("/selectGoodsBySpuId/{spuId}")
    public Result selectGoodsBySpuId(@PathVariable String spuId){
        Goods goods = spuService.selectGoodsBySpuId(spuId);
        return new Result(true,StatusCode.OK,"根据spu_id查询商品信息成功",goods);
    }

    /**
     * 更新商品
     * @param goods
     * @return
     */
    @PostMapping("/updateGoods")
    public Result updateGoods(@RequestBody Goods goods){
        spuService.updateGoods(goods);
        return new Result(true,StatusCode.OK,"修改商品信息成功");
    }

    /**
     * 商品审核
     * @param id
     * @return
     */
    @PutMapping("/auditGoods/{id}")
    public Result auditGoods(@PathVariable String id){
        spuService.auditGoods(id);
        return new Result(true,StatusCode.OK,"商品审核成功");
    }

    /**
     * 商品下架
     * @param id
     * @return
     */
    @PutMapping("/pull/{id}")
    public Result pull(@PathVariable String id){
        spuService.pull(id);
        return new Result(true,StatusCode.OK,"商品下架成功");
    }
    /**
     * 商品上架
     * @param id
     * @return
     */
    @PutMapping("/put/{id}")
    public Result put(@PathVariable String id){
        spuService.put(id);
        return new Result(true,StatusCode.OK,"商品上架成功");
    }
    /**
     * 逻辑删除
     * @param id
     * @return
     */
    @PutMapping("/deleteLogic/{id}")
    public Result deleteLogic(@PathVariable String id){
        spuService.deleteLogic(id);
        return new Result(true,StatusCode.OK,"商品逻辑删除成功");
    }
    /**
     * 恢复逻辑删除的数据
     * @param id
     * @return
     */
    @PutMapping("/restore/{id}")
    public Result restore(@PathVariable String id){
        spuService.restore(id);
        return new Result(true,StatusCode.OK,"恢复逻辑删除的数据成功");
    }
    /**
     * 物理删除
     * @param id
     * @return
     */
    @PutMapping("/realDelete/{id}")
    public Result realDelete(@PathVariable String id){
        spuService.realDelete(id);
        return new Result(true,StatusCode.OK,"物理删除数据成功");
    }
    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Spu> spuList = spuService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",spuList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id){
        Spu spu = spuService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",spu);
    }


    /***
     * 新增数据
     * @param spu
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Spu spu){
        spuService.add(spu);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Spu spu,@PathVariable String id){
        spu.setId(id);
        spuService.update(spu);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Spu> list = spuService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Spu> pageList = spuService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


}
