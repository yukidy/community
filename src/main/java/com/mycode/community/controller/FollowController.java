package com.mycode.community.controller;

import com.mycode.community.entity.Event;
import com.mycode.community.entity.Page;
import com.mycode.community.entity.User;
import com.mycode.community.event.EventProducer;
import com.mycode.community.service.FollowService;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;


    /**
     *  关注和取消关注是一个异步的操作
     */

    // 此处应有拦截器
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow (int entityType, int entityId) {

        // 获取当前用户
        User user = holder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注!");
    }


    // 此处应有拦截器
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow (int entityType, int entityId) {

        // 获取当前用户
        User user = holder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注!");
    }

    // 查找用户关注列表
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees (@PathVariable("userId") int userId, Page page, Model model) {
        // 判断用户是否存在
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        // 将用户装入Model
        model.addAttribute("user", user);

        // 设置分页
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        // 获取关注列表
        List<Map<String, Object>> followeeList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followeeList != null) {
            // 获取当前访问用户对列表用户的关注情况,遍历followeeList
            for (Map<String, Object> map : followeeList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("followeeList", followeeList);
        return "/site/followee";
    }

    // 判断当前用户是否关注某用户
    private boolean hasFollowed (int userId) {
        // 是否登录，未登录默认false
        if (holder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(holder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
    }

    // 查找用户粉丝列表
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers (@PathVariable("userId") int userId, Page page, Model model) {

        // 判断用户是否存在
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        // 获取关注列表
        List<Map<String, Object>> followerList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followerList != null) {
            // 获取当前访问用户对列表用户的关注情况,遍历followeeList
            for (Map<String, Object> map : followerList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("followerList", followerList);
        return "/site/follower";
    }

}
