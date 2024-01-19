package com.jgs.collegeexamsystemback.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;


import java.util.Collection;

/**
 * @author Administrator
 * @version 1.0
 * @description 自定义手机登录验证token
 * @date 2023/8/4 0004 11:00
 */
@Getter
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 987654L;
    private final String phone;
    private final String code;

    public SmsAuthenticationToken(String phone,String code){
        super(null);
        this.phone = phone;
        this.code = code;
        this.setAuthenticated(false);
    }
    public SmsAuthenticationToken(String phone, String code, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.phone = phone;
        this.code = code;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.code;
    }

    @Override
    public Object getPrincipal() {
        return this.phone;
    }
}
