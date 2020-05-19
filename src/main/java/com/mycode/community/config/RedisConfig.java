package com.mycode.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate (RedisConnectionFactory factory) {
        // redisTemplate要想有访问redis数据库的能力，需要创建连接
        // 连接是由连接工厂创建的:RedisConnectionFactory，spring容器会自动装配

        // 实例化bean
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 连接工厂
        redisTemplate.setConnectionFactory(factory);

        // 配置redisTemplate序列化的方式（数据转换的方式）
        // 序列化 (Serialization)是将对象的状态信息转换为可以存储或传输的形式的过程
        // 因为程序是java程序，得到的数据时java类型的数据，最终需要将数据存储到redis中去，
        // 需要指定一种序列化的方式，或者说数据转换的方式

        // 设置key的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());

        // 设置普通value的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());

        // 设置hash的key的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        // 设置hash的value的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        // 做完配置之后触发该方法生效
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}
