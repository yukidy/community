package com.mycode.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

//通常配置类使用@Configuration，程序的入口配置类使用@SpringBootApplication
@Configuration
public class AlphaConfig {

    /**
     * 将java自带的SimpleDateFormat方法装配到bean中
     * 因为项目中基本上的时间格式都一样此时我们将这个SimpleDateFormat实例化一次装配到bean中
     * 后可反复使用
     */
    @Bean
    public SimpleDateFormat simpleDateFormat() {    //表示该方法返回的对象将被装配到容器中
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
