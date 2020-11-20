package com.wechatshop.controller;

import com.wechatshop.entity.*;
import com.wechatshop.service.ShoppingCartService;
import com.wechatshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShoppingCartController {
    private ShoppingCartService shoppingCartService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }


    @GetMapping("/shoppingCart")
    public PageResponse<ShoppingCartData> getShoppingCart(@RequestParam("pageNum") int pageNum, @RequestParam("pageSize") int pageSize) {
        return shoppingCartService.getShoppingCartOfUser(UserContext.getCurrentUser().getId(), pageNum, pageSize);
    }

    @DeleteMapping("/shoppingCart/{goodsId}")
    public Object deleteGoodsInShoppingCart(@PathVariable("goodsId") long goodsId) {
            return ResponseData.of(shoppingCartService.deleteShoppingCartByGoodsId(goodsId, UserContext.getCurrentUser().getId()));
    }


    @PostMapping("/shoppingCart")
    public Object addToShopingCart(@RequestBody AddToShoppingCartRequest request) {
            return ResponseData.of(shoppingCartService.addToShoppingCart(request));
    }


    public static class AddToShoppingCartRequest {
        List<AddToShoppingCartItem> goods;

        public List<AddToShoppingCartItem> getGoods() {
            return goods;
        }

        public void setGoods(List<AddToShoppingCartItem> goods) {
            this.goods = goods;
        }
    }

    public static class AddToShoppingCartItem {
        //添加商品的id
        long id;
        //添加商品的数量
        int number;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}
