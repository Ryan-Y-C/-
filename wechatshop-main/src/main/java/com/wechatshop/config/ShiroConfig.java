package com.wechatshop.config;

import com.wechatshop.service.ShiroRealm;
import com.wechatshop.service.UserLoginInterceptor;
import com.wechatshop.service.UserService;
import com.wechatshop.service.VerificationCodeCheckService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class ShiroConfig implements WebMvcConfigurer {
    private UserService userService;

    @Value("wachatshop.redis.host")
    private String host;

    @Autowired
    public ShiroConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginInterceptor(userService));
    }

    @Bean
    public UserLoginInterceptor userLoginInterceptor(UserService userService) {
        return new UserLoginInterceptor(userService);
    }
    @Bean(name = "shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, ShiroLoginFilter shiroLoginFilter) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //设置登录接口和权限
        Map<String, String> pattern = new HashMap<>();
        pattern.put("/api/v1/code", "anon");
        pattern.put("/api/v1/login", "anon");
        pattern.put("/api/v1/status", "anon");
        pattern.put("/api/v1/logout", "anon");
        pattern.put("/api/v1/testRpc", "anon");
        //非匿名进行拦截
        pattern.put("/**", "authc");

        Map<String, Filter> filerMap = new LinkedHashMap<>();
        filerMap.put("shiroLoginFilter", shiroLoginFilter);
        shiroFilterFactoryBean.setFilters(filerMap);

        shiroFilterFactoryBean.setFilterChainDefinitionMap(pattern);
        return shiroFilterFactoryBean;
    }
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SessionsSecurityManager securityManager(ShiroRealm shiroRealm, RedisCacheManager redisCacheManager) {
        //提供session和coke
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        securityManager.setRealm(shiroRealm);
        securityManager.setCacheManager(redisCacheManager);
        securityManager.setSessionManager(new DefaultWebSessionManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }


    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager);
        return redisCacheManager;
    }

    @Bean
    public ShiroRealm shiroRealm(VerificationCodeCheckService verificationCodeCheckService) {
        return new ShiroRealm(verificationCodeCheckService);
    }

}
