package com.mycode.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * spring核心类，配置类
 */
@SpringBootApplication
public class CommunityApplication {

	// @PostConstruct主要用来管理bean的生命周期，在bean初始化时，有该注解的方法就会被调用
	// 它会在构造器调用以后被执行，通常就是初始化方法
	@PostConstruct
	public void init () {
		// 解决netty冲突问题
		// see Netty4Utils setAvailableProcessors
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		//spring应用启动了，底层启动了Tomcat、自动的创建了spring容器
		//在spring容器启动之后，会自动的扫描某些包下的bean，将这些bean装配到容器当中
		//1、装配的bean为配置类所在的包以及包下的bean
		//2、包下的类需要有@Controller注解才会被扫描到，与Controller等价的一些注解同样可以：Service、Repository、Component
		//事实上，Controller、Service、Repository都由Component实现的，所以可以被自动扫描到
		//区别在于语义上的区别：
		// 若是开发业务组件，最好使用Service注解表示
		// 若是开发数据库访问的组件，最好使用Repository注解表示，
		// 使用mybatis需要使用@mapper注解，否则找不到响应的xml，使用Repository可有可无，用来声明bean
		// 若是开发处理请求的组件，最好使用Controller注解表示
		// 如果开发的类在任何地方（数据库、业务、请求等）都能用，则可以使用Component
		SpringApplication.run(CommunityApplication.class, args);
	}

}
