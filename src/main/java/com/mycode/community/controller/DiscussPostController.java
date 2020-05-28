package com.mycode.community.controller;

import com.mycode.community.entity.*;
import com.mycode.community.event.EventProducer;
import com.mycode.community.service.CommentService;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer producer;

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

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());

        producer.fireEvent(event);

        // 失败或者报错，将来会统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");

    }

    // 通常根据id查找的数据，习惯将参数拼到路径当中 /detail/{discussPostId}
    // 评论数据
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDetailDiscussPost (@PathVariable("discussPostId") int discussPostId,
                                        Model model, Page page) {
        // 帖子
        DiscussPost post = postService.getDiscussPost(discussPostId);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞
        long likeCount = likeService.findLikeEntityCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);

        // 点赞状态,未登录不给点赞权限
        int likeStatus = holder.getUser() == null ? 0 :
                likeService.findLikeEntityStatus(holder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", likeStatus);

        // 帖子和作者可以通过关联查询，效率会更高
        // 但同时也会带来用户和帖子的数据层的一些耦合，有好处也有坏处
        // 这里分开查询，可用redis替换，弥补效率问题，让代码更加清晰简洁

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：给帖子的评论
        // 回复：给评论的评论

        // 评论的列表
        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        // 评论的显示对象的列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        if (commentList != null) {

            for (Comment comment : commentList) {
                // 一个评论的View Object 显示
                Map<String, Object> commentVo = new HashMap<>();
                // 评论内容
                commentVo.put("comment", comment);
                // 评论人
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // 点赞
                likeCount = likeService.findLikeEntityCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 点赞状态,未登录不给点赞权限
                likeStatus = holder.getUser() == null ? -1 :
                        likeService.findLikeEntityStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表/不做分页处理 Integer.MAX_VALUE 有多少显示多少，不限制条数
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复的View Object列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复内容
                        replyVo.put("reply", reply);
                        // 回复人
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        // 点赞
                        likeCount = likeService.findLikeEntityCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // 点赞状态,未登录不给点赞权限
                        likeStatus = holder.getUser() == null ? -1 :
                                likeService.findLikeEntityStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }

        }

        model.addAttribute("commentVoList", commentVoList);

        return "/site/discuss-detail";

    }

    /**
     * 帖子置顶
     * @param postId
     * @return
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop (int postId) {

        postService.setDiscussPostType(postId, POST_TYPE_TOP);

        // 触发发帖事件（帖子修改-ES的数据也需要变动）
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        producer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 帖子加精
     * @param postId
     * @return
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful (int postId) {

        postService.setDiscussPostStatus(postId, POST_STATUS_WONDERFUL);

        // 触发发帖事件（帖子修改-ES的数据也需要变动）
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        producer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 帖子删除
     * @param postId
     * @return
     */
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete (int postId) {

        postService.setDiscussPostStatus(postId, POST_STATUS_DELETE);

        // 触发发帖事件（帖子修改-ES的数据也需要变动）
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        producer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
