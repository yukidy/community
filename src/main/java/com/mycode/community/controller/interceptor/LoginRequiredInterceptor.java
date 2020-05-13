package com.mycode.community.controller.interceptor;

import com.mycode.community.annotation.LoginRequired;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 定义拦截器
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder holder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断handler拦截的目标是否是一个方法
        if (handler instanceof HandlerMethod) {
            // 确定了是方法，转型成HandlerMethod
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取拦截到的Method的对象
            Method method = handlerMethod.getMethod();
            // 尝试去取LoginRequired注解的方法
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && holder.getUser() == null) {
                // 重定向到登录界面
                response.sendRedirect(request.getContextPath() + "/login");
                // 拒绝后续的请求
                return false;
            }
        }

        return true;
    }
}
