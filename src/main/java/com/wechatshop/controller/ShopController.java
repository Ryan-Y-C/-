package com.wechatshop.controller;

import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.MessageResponse;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.entity.Response;
import com.wechatshop.generator.Shop;
import com.wechatshop.service.ShopService;
import com.wechatshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@RequestMapping("/api/v1")
public class ShopController {
    private ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/shop")
    public PageResponse<Shop> getShop(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize) {
        return shopService.getShopByUserId(UserContext.getCurrentUser().getId(), pageNum, pageSize);
    }

    @PostMapping("/shop")
    public Response<Shop> createdShop(@RequestBody Shop shop, HttpServletResponse servletResponse) {
        clean(shop);
        servletResponse.setStatus(HttpServletResponse.SC_CREATED);
        return Response.of(shopService.createdShop(shop, UserContext.getCurrentUser().getId()));
    }

    private void clean(Shop shop) {
        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
        shop.setOwnerUserId(null);
        shop.setOwnerUserId(1L);
    }

    @PatchMapping("/shop/{id}")
    public Object updateShop(@PathVariable("id") long id, @RequestBody Shop shop, HttpServletResponse response) {
        shop.setId(id);
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            return Response.of(shopService.updateShop(shop, UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return MessageResponse.of(e.getMessage());
        }
    }

    @DeleteMapping("/shop/{id}")
    public Object deleteShop(@PathVariable("id") long shopId, HttpServletResponse response) {
        try {
            return Response.of(shopService.deleteShop(shopId, UserContext.getCurrentUser().getId()));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            throw HttpException.forbidden(e.getMessage());
        }
    }
}
