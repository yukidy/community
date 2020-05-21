package com.mycode.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
        public void testKafka () {

        // 发消息
        kafkaProducer.sendMessage("test1", "你好！");
        kafkaProducer.sendMessage("test1", "在吗？");

        // 消息发送完后，希望程序不立即结束，等待消费者消费的过程
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}

/**
 * 封装生产者bean
 *
 * 生产者发消息是主动去发送的，什么时候发送消息，就什么时候调用
 */
@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage (String topic, String content) {
        kafkaTemplate.send(topic, content);
    }

}

/**
 * 封装消费者bean
 *
 * 消费者处理消息时被动的，只要一有消息，就会自动被处理，但是这个过程可能会略有延迟
 * 在很多消息在队列时，想要处理到你期望的那条消息，需要略微排队等候。
 */
@Component
class KafkaConsumer {

    // kafka启动后，spring会自动的去监听这些声明的主题
    // 就是以一个消费者的身份，有一个线程在那，试图去读取数据，如果没有消息就阻塞，有消息则立刻去读取
    // 读取到数据后会被自动封装到ConsumerRecord中
    @KafkaListener(topics = {"test1"})
    public void handleMessage (ConsumerRecord record) {

        System.out.println(record.value());

    }

}