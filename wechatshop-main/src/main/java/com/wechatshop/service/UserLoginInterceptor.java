package com.wechatshop.service;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class UserLoginInterceptor implements HandlerInterceptor {

    private UserService userService;

    @Autowired
    public UserLoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        System.out.println(request.getServletPath());
        Object tel = SecurityUtils.getSubject().getPrincipal();
        if (tel != null) {
            //当登录时获取数据库存储的用户并且将该用户插入userContext
            userService.getUserByTel(tel.toString()).ifPresent(UserContext::setCurrentUser);
        }
//        if (request.getServletPath().equals("/api/v1/status")) {
//
//        }

        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clearCurrentUser();
    }
}
