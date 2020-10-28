package com.wechatshop.controller;

import com.wechatshop.entity.LoginResponse;
import com.wechatshop.entity.TelAndCode;
import com.wechatshop.service.AuthService;
import com.wechatshop.service.TelVerificitonService;
import com.wechatshop.service.UserContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;
    private final TelVerificitonService telVerificitonService;

    @Autowired
    public AuthController(AuthService authService, TelVerificitonService telVerificitonService) {
        this.authService = authService;
        this.telVerificitonService = telVerificitonService;
    }

    @PostMapping("/code")
    public void code(@RequestBody TelAndCode telAndCode, HttpServletResponse response) {
        if (telVerificitonService.verifyTelParameter(telAndCode)) {
            authService.sendVerificationCode(telAndCode.getTel());
        } else {
            response.setStatus(SC_BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public void login(@RequestBody TelAndCode telAndCode) {
        UsernamePasswordToken token =
                new UsernamePasswordToken(telAndCode.getTel(), telAndCode.getCode());
        token.setRememberMe(true);
        SecurityUtils.getSubject().login(token);
        System.out.println(token.toString());

    }
    @GetMapping("/logout")
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @GetMapping("/status")
    public Object loginStatus() {
        if (UserContext.getCurrentUser() != null) {
            System.out.println(SecurityUtils.getSubject().getPrincipal());
            return LoginResponse.login(UserContext.getCurrentUser());
        } else {
            return LoginResponse.notLogin();
        }
    }
}
