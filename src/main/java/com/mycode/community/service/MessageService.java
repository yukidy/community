package com.mycode.community.service;

import com.mycode.community.dao.MessageMapper;
import com.mycode.community.entity.Message;
import com.mycode.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations (int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount (int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters (String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findtLetterCount (String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount (int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int addMessage (Message message) {

        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.sensitivefilter(message.getContent()));
        return messageMapper.insertMessage(message);

    }

    // 将未读消息变成已读消息
    public int readMessage (List<Integer> ids) {
        // status:0 未读、status:1 已读、status:2 删除
        return messageMapper.updateMessageStatus(ids, 1);
    }

    // 查询最新的通知
    public Message findLatestNotice (int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    // 查询某主题总的消息数量
    public int findNoticeCount (int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnhreadCount (int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    // 查找某主题下的消息
    public List<Message> findNotices (int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

}
