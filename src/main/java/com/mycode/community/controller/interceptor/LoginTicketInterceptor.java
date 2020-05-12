package com.mycode.community.controller.interceptor;

import com.mycode.community.entity.LoginTicket;
import com.mycode.community.entity.User;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CookieUtil;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder holder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取和封装名为“ticket”的cookie的值
        // 从cookie中获取登录凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket =  userService.getLoginTicket(ticket);
            // 检查凭证是否有效:是否失效、过期时间是否比当前时间多
            if (loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户:获取到的用户信息会在后面用到，所以暂存用户信息
                // 如何存储user？
                //      服务器在处理这次请求其实是处于多线程的环境，在存储user时要考虑多线程的情况
                //      如果只是简单的存储到了一个工具或则是容器当中，在并发情况下，这个变量是会产生冲突的
                // 考虑线程隔离，每个线程单独存储一份，互相不干扰
                holder.setUsers(user);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        //得到当前线程持有的User
        User user = holder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        holder.clear();

    }
}
