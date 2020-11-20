package com.wechatshop.service;

import com.api.DataStatus;
import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.generator.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {
    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private Shop shop;
    @Mock
    private Goods goods;
    @InjectMocks
    private GoodsService goodsService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        UserContext.setCurrentUser(user);
        lenient().when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
    }

    @AfterEach
    void clearUserContext() {
        UserContext.setCurrentUser(null);
    }

    @Test
    void createGoodsSucceedIfUserIsOwner() {
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.insert(goods)).thenReturn(123);
        assertEquals(goods, goodsService.createdGoods(goods));
        verify(goods).setId(123L);
    }

    @Test
    void createGoodsFailedIfUserIsNotOwner() {
        when(shop.getOwnerUserId()).thenReturn(2L);
        HttpException thrownException = assertThrows(HttpException.class, () -> goodsService.createdGoods(goods));
        assertEquals(HttpStatus.FORBIDDEN.value(), thrownException.getStatusCode());
    }

    @Test
    void throwExceptionIfGoodsNotFound() {
        long goodsToBeDeleted = 123L;
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.selectByPrimaryKey(goodsToBeDeleted)).thenReturn(null);
        HttpException thrownException = assertThrows(HttpException.class, () -> goodsService.deleteGoodsById(goodsToBeDeleted));
        assertEquals(HttpStatus.NOT_FOUND.value(), thrownException.getStatusCode());
    }

    @Test
    void deleteGoodsThrowExceptionIfUserIsNotOwner() {
        long goodsToBeDeleted = 123L;
        when(shop.getOwnerUserId()).thenReturn(2L);
        HttpException thrownException = assertThrows(HttpException.class, () -> goodsService.deleteGoodsById(goodsToBeDeleted));
        assertEquals(HttpStatus.FORBIDDEN.value(), thrownException.getStatusCode());
    }

    @Test
    void deleteGoodsSucceed() {
        long goodsToBeDeleted = 123L;
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.selectByPrimaryKey(goodsToBeDeleted)).thenReturn(goods);
        goodsService.deleteGoodsById(goodsToBeDeleted);
        verify(goods).setStatus(DataStatus.DELETED.getName());
    }

    @Test
    void goodsSucceedWithNullShopId() {
        int pageNumber = 5;
        int pageSize = 10;

        List<Goods> mockData = Mockito.mock(List.class);
        when(goodsMapper.countByExample(any())).thenReturn(55L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);
        PageResponse<Goods> result = goodsService.getGoods(pageNumber, pageSize, null);
        assertEquals(6, result.getTotalPage());
        assertEquals(5, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(mockData, result.getData());
    }

    @Test
    void goodsSucceedWithNonNullShopId() {
        int pageNumber = 5;
        int pageSize = 10;

        List<Goods> mockData = Mockito.mock(List.class);
        when(goodsMapper.countByExample(any())).thenReturn(100L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);
        PageResponse<Goods> result = goodsService.getGoods(pageNumber, pageSize, 123);
        assertEquals(10, result.getTotalPage());
        assertEquals(5, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(mockData, result.getData());
    }
}
