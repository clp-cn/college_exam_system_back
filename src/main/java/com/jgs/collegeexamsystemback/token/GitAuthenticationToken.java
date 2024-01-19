package com.jgs.collegeexamsystemback.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author Administrator
 * @version 1.0
 * @description 自定义git登录验证token
 * @date 2023/8/4 0004 21:34
 */
@Getter
public class GitAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 987655L;
    private final String gitee;
    public GitAuthenticationToken(String gitee) {
        super(null);
        this.gitee = gitee;
        this.setAuthenticated(false);
    }

    public GitAuthenticationToken(String gitee,Collection<? extends GrantedAuthority> authorities){
        super(authorities);
        this.gitee = gitee;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.gitee;
    }
}
