package com.mycode.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
// 启用spring scheduler定时任务，默认不启用定时任务线程池
// 启用后才能使用
@EnableScheduling
// 能够让方法通过注解的方式，被当做线程体，被线程池异步的调用
@EnableAsync
public class ThreadPoolConfig {



}
