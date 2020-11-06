package com.wechatshop.service;

import com.wechatshop.dao.ShoppingCartQueryMapper;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.entity.ShoppingCartData;
import com.wechatshop.entity.ShoppingCartGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {
    private ShoppingCartQueryMapper shoppingCartQueryMapper;

    @Autowired
    public ShoppingCartService(ShoppingCartQueryMapper shoppingCartQueryMapper) {
        this.shoppingCartQueryMapper = shoppingCartQueryMapper;
    }

    public PageResponse<ShoppingCartData> getShoppingCartOfUser(Long userId, int pageNum, int pageSize) {

        int offset = (pageNum - 1) * pageSize;
        int totalNum = shoppingCartQueryMapper.countHowManyShopsInUserShoppingCart(userId);

        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        List<ShoppingCartData> pageData =
                shoppingCartQueryMapper.selectShoppingCartDataByUserId(userId, pageSize, offset)
                        .stream()
                        .collect(Collectors.groupingBy(shoppingCartData -> shoppingCartData.getShop().getId()))
                        .values()
                        .stream()
                        .map(this::merge)
                        .collect(Collectors.toList());
        return PageResponse.pageData(pageNum, pageSize, totalPage, pageData);
    }

    private ShoppingCartData merge(List<ShoppingCartData> goodsOfSameShop) {
        ShoppingCartData result = new ShoppingCartData();
        result.setShop(goodsOfSameShop.get(0).getShop());
        List<ShoppingCartGoods> goods = goodsOfSameShop
                .stream()
                .map(ShoppingCartData::getGoods).flatMap(List::stream).collect(Collectors.toList());
        result.setGoods(goods);
        return result;
    }
}
