package com.wechatshop.dao;

import com.wechatshop.generator.Goods;
import com.wechatshop.generator.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.wechatshop.entity.DataStatus.DELETE_STATUS;

@Service
public class GoodsDao {
    private GoodsMapper goodsMapper;

    @Autowired
    public GoodsDao(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    public Goods insertGoods(Goods goods) {
        long goodsId = goodsMapper.insert(goods);
        goods.setId(goodsId);
        return goods;
//        try(SqlSession session = sqlSessionFactory.openSession()){
//            GoodsMapper mapper = session.getMapper(GoodsMapper.class);
//
//        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Goods deleteGoods = goodsMapper.selectByPrimaryKey(goodsId);
        if (deleteGoods == null) {
            throw new ResourceNotFoundException("商品未找到！");
        }
        deleteGoods.setStatus(DELETE_STATUS);
        goodsMapper.updateByPrimaryKey(deleteGoods);
        return deleteGoods;
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
