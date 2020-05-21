package com.mycode.community.event;

import com.alibaba.fastjson.JSONObject;
import com.mycode.community.entity.Event;
import com.mycode.community.entity.Message;
import com.mycode.community.service.MessageService;
import com.mycode.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    // 在处理发布的消息的过程中，可能会发生一个问题，用日志记录
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private MessageService messageService;

    /**
     *  这里可以写多个方法分别处理三个不同的事件
     *  但也可以写一个方法共同处理，点赞、评论、关注，有很大的相似度，
     *  使用一个方法，监听三个topic
     */

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleMessages (ConsumerRecord record) {

        // 空值处理
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        // 数据格式处理
        // json -> javabean
        // JSON.parseObject(String text, Class clazz)，
        // 作用就是将指定的JSON字符串转换成自己的实体类的对象
        // 即：JSONObject.parseObject(键值对格式的str字符串, javabean类)

        // 此外 JSONObject.parseObject(str)
        // 将JSON字符串（str）转换成JSON对象 jsonObject 。注意str一定得是以键值对存在的JSON字符串

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        // 发送站内通知
        // 本质，构造一个message插入Message表中
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        // message.setStatus(); 默认是 0
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // 判断event中map是否存在
        if (!event.getData().isEmpty()) {
            // 遍历map,entry同时获得key和value，性能最优
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        // 存储message信息
        messageService.addMessage(message);
    }

}
