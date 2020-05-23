package com.mycode.community.controller.interceptor;

import com.mycode.community.entity.User;
import com.mycode.community.service.MessageService;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  显示未读总消息
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {


    @Autowired
    private HostHolder holder;

    @Autowired
    private MessageService messageService;

    // 在调用Controller之后，生成template模板之前，拦截并添加未读总消息的信息
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        User user = holder.getUser();
        if (user != null && modelAndView != null) {

            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnhreadCount(user.getId(), null);

            modelAndView.addObject("allMessageCounts", letterUnreadCount + noticeUnreadCount);

        }

    }
}
