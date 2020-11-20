package com.wechatshop.entity;

import com.api.generator.Order;
import com.wechatshop.generator.Shop;

import java.util.List;

public class OrderResponse extends Order {
    private Shop shop;
    private List<GoodsWithNumber> goodsList;

    public OrderResponse() {
    }
    public OrderResponse(Order order) {
        this.setId(order.getId());
        this.setUserId(order.getUserId());
        this.setTotalPrice(order.getTotalPrice());
        this.setAddress(order.getAddress());
        this.setExpressCompany(order.getExpressCompany());
        this.setExpressId(order.getExpressId());
        this.setStatus(order.getStatus());
        this.setCreatedAt(order.getCreatedAt());
        this.setUpdatedAt(order.getUpdatedAt());
//        this.setShopId(order.getShopId());
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public List<GoodsWithNumber> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<GoodsWithNumber> goodsList) {
        this.goodsList = goodsList;
    }
}
