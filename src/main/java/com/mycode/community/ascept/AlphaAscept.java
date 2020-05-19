package com.mycode.community.ascept;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * aop事例
 */
//@Component  // 表示是一个组件，需要加入spring容器
//@Aspect // 表示是一个切面组件
public class AlphaAscept {

    // 切点joinPoint，声明织入代码到哪个目标对象的哪个位置
    // execution(* com.mycode.community.service.*.*(..)) 选中一切业务目标对象
    // * 返回值、 com.mycode.community.service.* 表示项目service包下的所有类、 *(..)的所有方法及方法参数
    @Pointcut("execution(* com.mycode.community.service.*.*(..))")
    public void pointcut () {

    }

    // 通知advice，实现具体的系统逻辑

    // 表示在前面织入代码
    @Before("pointcut()")
    public void before () {
        System.out.println("before");
    }

    // 表示在后面织入代码
    @After("pointcut()")
    public void after () {
        System.out.println("after");
    }

    // 表示在有返回值之后织入代码
    @AfterReturning("pointcut()")
    public void afterReturning () {
        System.out.println("afterReturning");
    }

    // 表示在抛异常之后织入代码
    @AfterThrowing("pointcut()")
    public void afterThrowing () {
        System.out.println("afterThrowing");
    }

    // 前后都织入逻辑代码:需要返回值和返回参数
    // ProceedingJoinPoint:目标织入的部位
    @Around("pointcut()")
    public Object around (ProceedingJoinPoint joinPoint) throws Throwable {

        System.out.println("around before");
        // 调用目标组件的方法，目标组件的方法可能会有返回值，obj接收
        Object obj = joinPoint.proceed();
        System.out.println("around after");

        // 程序在执行时会执行一个代理对象，逻辑被织入到代理对象里，用来代替原始对象
        // 而此处利用joinPoint去调用原始对象的方法
        // 就是说在Object obj = joinPoint.proceed()各加一段代码，就是在方法前和方法后织入代码
        // 解决都织入的问题

        return obj;

    }


}
