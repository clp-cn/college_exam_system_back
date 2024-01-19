package com.jgs.collegeexamsystemback.config;
import com.jgs.collegeexamsystemback.filter.JwtAuthenticationTokenFilter;
import com.jgs.collegeexamsystemback.filter.SmsAuthenticationFilter;
import com.jgs.collegeexamsystemback.handler.SGFailureHandler;
import com.jgs.collegeexamsystemback.handler.SGLogoutSuccessHandler;
import com.jgs.collegeexamsystemback.handler.SGSuccessHandler;
import com.jgs.collegeexamsystemback.provider.GitAuthenticationProvider;
import com.jgs.collegeexamsystemback.provider.SmsAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import javax.annotation.Resource;
/**
 * @author Administrator
 * @version 1.0
 * @description SecurityConfig
 * @date 2023/7/13 0013 13:36
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Resource
    private SmsAuthenticationProvider smsAuthenticationProvider;
    @Resource
    private GitAuthenticationProvider gitAuthenticationProvider;
    @Resource
    private SmsAuthenticationFilter smsAuthenticationFilter;
    @Resource
    private SGSuccessHandler sgSuccessHandler;
    @Resource
    private SGFailureHandler sgFailureHandler;
    @Resource
    private SGLogoutSuccessHandler sgLogoutSuccessHandler;
    @Resource
    private AccessDeniedHandler accessDeniedHandler;
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Resource
    private UserDetailsService userDetailsService;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }
    @Bean
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider(){
        UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>(userDetailsService);
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(wrapper);
        return provider;
    }
    /**
    * @description 身份认证接口
    * @returnType void
    * @author Administrator
    * @date  23:23
    */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
        auth.authenticationProvider(preAuthenticatedAuthenticationProvider());
        auth.authenticationProvider(smsAuthenticationProvider);
        auth.authenticationProvider(gitAuthenticationProvider);
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                // 配置认证成功处理器
                        .successHandler(sgSuccessHandler)
                // 配置认证失败处理器
                                .failureHandler(sgFailureHandler);
        http.logout()
                // 配置注销成功处理器
                        .logoutSuccessHandler(sgLogoutSuccessHandler);
        http.csrf().disable()   // 关闭csrf
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 不通过Session获取SecurityContext
                .and().authorizeRequests()
                .antMatchers("/system/user/login").anonymous() // 对于登录接口 允许匿名访问
                .antMatchers("/system/user/resetPassword/**").anonymous()
                .antMatchers("/system/user/getVerifyCode").anonymous()
                .antMatchers("/system/wx/getVerifyCode").anonymous()
                .antMatchers("/system/chat").anonymous()
                .antMatchers("/oauth/**").anonymous()
                .anyRequest().authenticated();  // 除上面外的所有请求全部需要鉴权认证
        // 添加过滤器
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(smsAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);
        // 添加异常处理器
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler);
        // 允许跨域
        http.cors();
    }
}
