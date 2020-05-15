package com.mycode.community.controller;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.User;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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
    private UserService userService;

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

    // 通常根据id查找的数据，习惯将参数拼到路径当中 /detail/{discussPostId}
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDetailDiscussPost (@PathVariable("discussPostId") int discussPostId,
                                        Model model) {
        // 帖子
        DiscussPost post = postService.getDiscussPost(discussPostId);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 帖子和作者可以通过关联查询，效率会更高
        // 但同时也会带来用户和帖子的数据层的一些耦合，有好处也有坏处
        // 这里分开查询，可用redis替换，弥补效率问题，让代码更加清晰简洁

        return "/site/discuss-detail";

    }

}
