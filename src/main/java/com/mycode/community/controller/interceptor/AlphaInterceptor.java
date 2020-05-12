package com.mycode.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 1、定义拦截器
 * 需要将拦截器注入到spring
 */
@Component
public class AlphaInterceptor implements HandlerInterceptor {   //实现HandlerInterceptor接口

    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    /**
     * HandlerInterceptor接口有三个方法
     *  这里将三个方法都实现，看看如何使用
     */

    // 在Controller执行之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 参数中有request请求，在controller接收请求之前，可以通过request对请求添加自己想要的内容，做一些修改

        logger.debug("preHandle:" + handler.toString());
        return true;
    }

    // 在调用完Controller之后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        // 参数中有ModelAndView，在Controller执行之后，表示主要的请求逻辑已经完成，前端控制器会通过model和view去模板引擎绚烂内容，返回页面
        // 在去模板引擎的过程中，你可能需要用到ModelAndView中的数据，或者是往里装入一些新数据

        logger.debug("postHandle:" + handler.toString());
    }


    // 在TemplateEngine之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion:" + handler.toString());
    }

    // Object handle 这个对象就是要拦截的目标
    // 访问login，以下是debug打印出的内容
    // preHandle:com.mycode.community.controller.LoginController#getLoginPage()
    // postHandle:com.mycode.community.controller.LoginController#getLoginPage()
    // afterCompletion:com.mycode.community.controller.LoginController#getLoginPage()

}
