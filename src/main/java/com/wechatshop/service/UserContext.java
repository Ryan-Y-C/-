package com.wechatshop.service;

import com.wechatshop.generator.User;

public class UserContext {
    //每个用户占有一个线程
    private static ThreadLocal<User> currentUser=new ThreadLocal<>();
    public static User getCurrentUser(){
        return currentUser.get();
    }
    public static void setCurrentUser(User user){
        currentUser.set(user);
    }
}
