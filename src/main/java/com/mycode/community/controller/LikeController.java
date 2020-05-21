package com.mycode.community.controller;

import com.mycode.community.entity.Event;
import com.mycode.community.entity.User;
import com.mycode.community.event.EventProducer;
import com.mycode.community.service.LikeService;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String giveLike (int entityType, int entityId, int entityUserId, int postId) {

        User user = holder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 点赞数
        long likeCount = likeService.findLikeEntityCount(entityType, entityId);
        // 当前用户点赞状态（1-已点赞/0-未点赞）
        int likeStatus = likeService.findLikeEntityStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        // 被赞进行通知，取消赞不通知
        if (likeStatus == 1) {

            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);

            eventProducer.fireEvent(event);

        }

        return CommunityUtil.getJSONString(0, null, map);
    }


}
