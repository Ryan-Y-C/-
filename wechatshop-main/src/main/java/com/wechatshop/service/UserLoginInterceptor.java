package com.wechatshop.service;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        //线程会复用
        //当线程1中保存用户A的信息，并且没有清除
        //线程1下次处理别的请求的时候会出现“串号”的情况
        UserContext.setCurrentUser(null);
    }
}
