package com.mycode.community.controller;

import com.mycode.community.entity.Comment;
import com.mycode.community.service.CommentService;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder holder;

    // 在评论完之后，我们希望的还是返回这个帖子的详情页面，
    // 但是这个详情页面的路径是带有帖子id的，
    // 我们希望在重定向的地方是需要用到帖子id的，需要在添加的路径里也带上帖子id，将值传进来
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment (@PathVariable("discussPostId") int discussPostId, Comment comment) {

        // 用户没有登录，评论会报错，但是后面会做统一的异常处理
        // 而且后面会做统一的权限的认证，没有登录就访问不了这个方法
        comment.setUserId(holder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
