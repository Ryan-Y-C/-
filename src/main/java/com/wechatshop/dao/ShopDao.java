package com.wechatshop.dao;

import com.wechatshop.generator.Shop;
import com.wechatshop.generator.ShopMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopDao {
    private SqlSessionFactory sessionFactory;

    @Autowired
    public ShopDao(SqlSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Shop findShopById(Long shopId) {
        try(SqlSession session = sessionFactory.openSession()){
            final ShopMapper shopMapper = session.getMapper(ShopMapper.class);
            return shopMapper.selectByPrimaryKey(shopId);
        }
    }
}
