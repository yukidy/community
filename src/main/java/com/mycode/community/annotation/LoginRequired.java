package com.mycode.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：LoginRequired
 * 作用：检查登录状态
 */
// 声明元注解
@Target(ElementType.METHOD)
// 元注解RetentionPolicy，表明注解的生命周期：
//
// 1、SOURCE：在原文件中有效，被编译器丢弃。
// 2、CLASS：在class文件有效，可能会被虚拟机忽略,编译时有效。
// 3、RUNTIME：在运行时有效。
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

    // 这个注解里，其实只是起到一个标识的作用
    // 标记之后，必须登录时才能访问，不登录不能访问

    //接下来就是去标记需要登录后才能访问的功能

}
