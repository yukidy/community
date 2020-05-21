package com.mycode.community.ascept;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  统一记录日志
 */

@Component
@Aspect
public class ServiceLogAscept {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAscept.class);

    @Pointcut("execution(* com.mycode.community.service.*.*(..))")
    public void pointcut () {}

    @Before("pointcut()")
    public void before (JoinPoint joinPoint) {

        // 声明日志格式：
        // 用户[1.2.3.4],在[xxx]，访问了[com.mycode.community.service.*.*(..))]

        // 获取用户ip,需要用到request
        // 获取request
        // 如何在增方法中获取request对象？ 不能简单的在参数中声明一个request
        // 利用RequestContextHolder工具类的静态方法：返回值类型是RequestAttributes
        // 可以强制转型成一个子类型ServletRequestAttributes，功能更多
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();


        // 在开发完消息队列后，新增了生产者和消费者
        // 在此之前，所有对service的方法都是有Controller调用，有用户的操作
        // 但新增了Consumer之后，消费者有调用service，此时出现了特殊情况，没有用户去调用
        // requestAttributes无法获取，为null
        // 修改
        if (requestAttributes == null) {
            return;
        }
        HttpServletRequest request = requestAttributes.getRequest();


        // 获取发出请求的客户端的主机名
        String ip = request.getRemoteHost();
        // 获取发出请求的客户端的IP地址
        //String test = request.getRemoteAddr();

        // alphaConfig中配置好了SimpleDateFormat()的时间格式
        // 否则new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
        String now = new SimpleDateFormat().format(new Date());

        // 获取访问了哪个类哪个方法
        // getDeclaringTypeName:类名、getName：方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        logger.info(String.format("用户[%s],在[%s]，访问了[%s].", ip, now, target));

    }

}
