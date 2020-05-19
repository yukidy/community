package com.mycode.community.controller.advice;

import com.mycode.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ControllerAdvice Controller类的全局配置类
 *      什么都不加，这个类会扫描所有的bean，范围太大，通常需要进行一个限制范围
 *
 *  @ControllerAdvice(annotations = Controller.class)
 *      意思是只去扫描带有Controller注解的那些bean
 *
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 处理所有异常的方法
     *
     * @ExceptionHandler({Exception.class})
     *      处理捕获到的异常，在Controller出现异常后调用
     *
     * ({Exception.class})
     *      括号里写你需要处理哪些异常，大括号里可以写多个数据，处理多种异常
     *    Exception.class是所有异常的父类，表示处理所有异常
     */
    @ExceptionHandler({Exception.class})
    public void handleEcxeption (Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 方法上可以带很多参数，去spring手册查找
        // 但是通常比较常用的是这三个参数，一般情况下处理异常足够
        // Exception e 用于接收Controller中的异常
        // 处理异常的过程中可能需要用到 request，response去处理请求和响应

        // 记录日志
        logger.error("服务器发生异常:" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            // 遍历异常的详细的栈信息
            // 每一个element记录的都是一条异常的信息
            logger.error(element.toString());
        }

        // 响应浏览器，重定向到错误页面
        // 普通请求：响应错误页面
        // 异步请求：就是ajax的，用户是希望我们返回的是一串json的提示信息

        String xRequestWith = request.getHeader("x-requested-with"); // 获取请求的方式，在请求头当中
        if ("XMLHttpRequest".equals(xRequestWith)) { // 异步请求
            // "application/json"表示向浏览器返回的字符串，浏览器会自动的转换成字符串
            // "application/plain"表示向浏览器返回的是一个普通的字符串，但是这个字符串可以是json格式
            // 浏览器得到以后需要人为的将这个字符串转换为js对象：$.parseJSON()这个方法
            // 这里使用application/plain，只要确保这个字符串时json格式的就行
            response.setContentType("application/plain;charset=utf-8");
            try {
                PrintWriter writer = response.getWriter();
                writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else { // 普通请求
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }


}
