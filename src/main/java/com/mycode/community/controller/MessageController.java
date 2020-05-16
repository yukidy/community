package com.mycode.community.controller;

import com.mycode.community.dao.MessageMapper;
import com.mycode.community.entity.Message;
import com.mycode.community.entity.Page;
import com.mycode.community.entity.User;
import com.mycode.community.service.MessageService;
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

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getConversationList (Model model, Page page) {

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(holder.getUser().getId()));

        // 会话列表
        User user = holder.getUser();
        List<Message> conversationsList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();

        if (conversationsList != null) {

            for(Message message : conversationsList) {
                Map<String, Object> map = new HashMap<>();
                // 会话，每个会话只返回一条最新的私信
                map.put("message", message);
                // 该会话所包含的私信数量
                int letterCount = messageService.findtLetterCount(message.getConversationId());
                map.put("letterCount", letterCount);
                // 该会话未读私信的数量
                int unreadLetterCount = messageService.findLetterUnreadCount(user.getId(), message.getConversationId());
                map.put("unreadCount", unreadLetterCount);
                // 会话人：与当前用户通信的用户信息
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
            model.addAttribute("conversations", conversations);
        }

        // 查询用户未读消息总数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("unreadCounts", letterUnreadCount);

        return "/site/letter";

    }

    // 私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail (@PathVariable("conversationId") String conversationId, Page page, Model model) {

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findtLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                // 显示发该条message消息的人
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设为已读：未读状态修改（进入详情私信列表，表示已读未读信息）
        List<Integer> ids = getReadLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getReadLetterIds (List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                // 判断私信：用户为收件人且status为0确定为未读消息
                if (holder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    // 将这条未读私信加入集合
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    private User getLetterTarget (String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        // 返回目标用户
        if (holder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter (String toName, String content) {

        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }

        Message message = new Message();
        message.setFromId(holder.getUser().getId());
        message.setToId(target.getId());

        // 规范conversation_id
        if (message.getFromId() > message.getToId()) {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        } else {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }

        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

}
