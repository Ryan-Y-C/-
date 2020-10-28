package com.wechatshop.config;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@Component
public class ShiroLoginFilter extends FormAuthenticationFilter{
    /**
     * 在访问controller前判断登录，返回401，进行重定向
     * @param request
     * @param response
     * @return true-继续执行，false-filter过滤器已经处理，不继续执行其他过滤器
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpServletResponse=(HttpServletResponse) response;
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(401);
        return false;
    }
}
