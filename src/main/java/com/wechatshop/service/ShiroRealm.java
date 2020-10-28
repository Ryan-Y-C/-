package com.wechatshop.service;


import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

public class ShiroRealm extends AuthorizingRealm {

    private final VerificationCodeCheckService verificationCodeCheckService;

    @Autowired
    public ShiroRealm(VerificationCodeCheckService verificationCodeCheckService) {
        this.verificationCodeCheckService = verificationCodeCheckService;
        this.setCredentialsMatcher((authenticationToken, authenticationInfo) -> new String ((char[]) authenticationToken.getCredentials()).equals(authenticationInfo.getCredentials()));

    }

    //当前用户是否有访问权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    //身份检验
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String tel = (String) authenticationToken.getPrincipal();
        //通过电话获取验证码
        String correctCode = verificationCodeCheckService.getCorrectCode(tel);
        return new SimpleAuthenticationInfo(tel, correctCode, getName());
    }
}
