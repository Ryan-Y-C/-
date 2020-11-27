package com.wechatshop.controller;

import com.api.exceptions.HttpException;
import com.wechatshop.entity.MessageResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ErrorHandlingController {
    //用于处理异常的方法
    //@ResponseBody将返回结果转换成json
    @ExceptionHandler(HttpException.class)
    public @ResponseBody
    MessageResponse onError(HttpServletResponse response, HttpException e){
        response.setStatus(e.getStatusCode());
        return MessageResponse.of(e.getMessage());

    }
}
