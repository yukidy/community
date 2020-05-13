package com.mycode.community.config;

import com.mycode.community.controller.interceptor.AlphaInterceptor;
import com.mycode.community.controller.interceptor.LoginRequiredInterceptor;
import com.mycode.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 2、配置拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 拦截器的配置类要求实现一个借口，而不是简单的注入一个@Bean

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor ticketInterceptor;

    @Autowired
    private LoginRequiredInterceptor requiredInterceptor;

    // 注册拦截器:实现addInterceptors方法
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 这样默认是拦截一切请求
        // registry.addInterceptor(alphaInterceptor);

        // excludePathPatterns表示排除掉一些不拦截的路径
        // 如静态资源：css，js，image不拦截，因为静态资源没有业务逻辑需要处理

        // /**表示所有的文件
        // 比如有一个Controller的访问路径/user，/user下有很多个方法，使用/user/**
        // /**/*.css表示静态资源目录下所有css文件

        // addPathPatterns表示明确一些需要拦截的路径
        // 假设我想拦截登录、注册
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
                .addPathPatterns("/register", "/login");

        // 登录成功后，所有的页面都需要拦截，显示用户信息
        registry.addInterceptor(ticketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        // 要求：对所有静态资源都不处理，而动态资源都处理
        // 动态资源在处理时，人为的筛选了带注解的那部分，其他的不管
        // 处理加快效率
        registry.addInterceptor(requiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }


}
