package com.mycode.community.controller;

import com.mycode.community.entity.Comment;
import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.Event;
import com.mycode.community.event.EventProducer;
import com.mycode.community.service.CommentService;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private EventProducer producer;

    @Autowired
    private DiscussPostService postService;

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

        // 触发评论事件

        // 评论-发送站内通知
        Event event = new Event();

        // 查询是对帖子的评论还是对评论的评论
        if (comment.getEntityType() == ENTITY_TYPE_POST) {

            // 获得被评论的帖子
            DiscussPost target = postService.getDiscussPost(comment.getEntityId());

            // 触发发帖事件
            Event postEvent = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(holder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            producer.fireEvent(postEvent);

            // //判断是否是给自己帖子或回复的评论（不触发）
            if (target.getUserId() == holder.getUser().getId()) {
                return "redirect:/discuss/detail/" + discussPostId;
            }
            // 获得被评论的帖子的作者id
            event.setEntityUserId(target.getUserId());

        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {

            // 获得被评论的评论
            Comment target = commentService.findCommentById(comment.getEntityId());
            // //判断是否是给自己帖子或回复的评论（不触发）
            if (target.getUserId() == holder.getUser().getId() || comment.getTargetId() == holder.getUser().getId()) {
                return "redirect:/discuss/detail/" + discussPostId;
            }
            // 获得被评论的用户id
            event.setEntityUserId(target.getUserId());

        }

        event.setTopic(TOPIC_COMMENT)
                .setUserId(holder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        // 触发事件
        producer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
