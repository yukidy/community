package com.mycode.community.controller;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.User;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService postService;

    @Autowired
    private HostHolder holder;

    @RequestMapping(path = "/post", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost (String title, String content) {

        User user = holder.getUser();
        if (user == null) {
            //做异步处理，返回json格式
            return CommunityUtil.getJSONString(403, "您还没登录哦!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        postService.addDiscussPost(discussPost);

        // 失败或者报错，将来会统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");

    }

}
