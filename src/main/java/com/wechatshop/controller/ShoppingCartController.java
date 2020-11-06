package com.wechatshop.controller;

import com.wechatshop.entity.PageResponse;
import com.wechatshop.entity.ShoppingCartData;
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
    public PageResponse<ShoppingCartData> getShopCart(@RequestParam("pageNum") int pageNum, @RequestParam("pageSize") int pageSize) {
        return shoppingCartService.getShoppingCartOfUser(UserContext.getCurrentUser().getId(), pageNum, pageSize);
    }


    @PostMapping("/shoppingCart")
    public void addToShopingCart(@RequestBody AddToShoppingCartItem request) {

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
        long id;
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
