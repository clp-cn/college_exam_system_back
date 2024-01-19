package com.jgs.collegeexamsystemback.config;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
* @description MybatisPlus配置类
* @returnType
* @author Administrator
* @date  20:42
*/
@Configuration
public class MybatisConfig {
    /**
    * @description 分页插件
    * @returnType com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
    * @author Administrator
    * @date  20:43
    */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return mybatisPlusInterceptor;
    }
}
