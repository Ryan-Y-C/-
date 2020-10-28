package com.wechatshop.service;

import com.wechatshop.WechatshopApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//junit5 spring扩展插件
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//创建集成测试数据库
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class GoodsIntegrationTest extends HttpUtils {
    @Test
    public void testCreateGoods() {

    }

    @Test
    public void testDeleteGoods() {

    }


}
