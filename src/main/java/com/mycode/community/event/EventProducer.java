package com.mycode.community.event;

import com.alibaba.fastjson.JSONObject;
import com.mycode.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent (Event event) { // 传入事件对象

        // 将事件发布到指定的主题
        // 主题:事件对象包含的所有数据
        // 将event转换为json字符串，消费者得到了json字符串就可以很容易的转换成event对象获得相应的数据
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));

    }


}
