package com.wechatshop.controller;


import com.api.data.PageResponse;
import com.wechatshop.entity.ResponseData;
import com.wechatshop.generator.Goods;
import com.wechatshop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;

@RestController
@RequestMapping("/api/v1")
public class GoodsController {
    private GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @DeleteMapping("/goods/{id}")
    public Object deleteGoods(@PathVariable("id") Long goodsId, HttpServletResponse httpServletResponse) {
            Goods goods = goodsService.deleteGoodsById(goodsId);
            httpServletResponse.setStatus(SC_NO_CONTENT);
            return ResponseData.of(goods);

    }

    @RequestMapping(value = "/goods/{id}", method = {RequestMethod.POST, RequestMethod.PATCH})
    public Object updateGoods(@PathVariable("id") long id, @RequestBody Goods goods, HttpServletResponse httpServletResponse) {
            Goods updateGoods = goodsService.updateGoods(id, goods);
            httpServletResponse.setStatus(SC_OK);
            return ResponseData.of(updateGoods);
    }

    @PostMapping("/goods")
    public Object createdGoods(@RequestBody Goods goods, HttpServletResponse httpServletResponse) {
        clean(goods);

            Goods goodsResponse = goodsService.createdGoods(goods);
            httpServletResponse.setStatus(SC_CREATED);
            return ResponseData.of(goodsResponse);

    }

    @GetMapping("/goods")
    public @ResponseBody
    PageResponse<Goods> getGoods(@RequestParam("pageNum") Integer pageNum,
                                 @RequestParam("pageSize") Integer pageSize,
                                 @RequestParam(value = "shopId", required = false) Integer shopId) {
        return goodsService.getGoods(pageNum, pageSize, shopId);

    }

    private void clean(Goods goods) {
        goods.setCreatedAt(new Date());
        goods.setUpdatedAt(new Date());
        goods.setId(null);
    }
}
