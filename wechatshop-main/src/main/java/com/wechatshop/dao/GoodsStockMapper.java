package com.wechatshop.dao;

import com.api.data.GoodsInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsStockMapper {
    int deductStock(GoodsInfo goodsInfo);
}
