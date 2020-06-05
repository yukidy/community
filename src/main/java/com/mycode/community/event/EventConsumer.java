package com.mycode.community.event;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.*;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.Event;
import com.mycode.community.entity.Message;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.service.ElasticsearchService;
import com.mycode.community.service.MessageService;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {

    // 在处理发布的消息的过程中，可能会发生一个问题，用日志记录
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService postService;

    @Autowired
    private ElasticsearchService elasticService;

    @Value(("${wk.image.storage}"))
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    // OSS
    @Value("${aliyun.key.access}")
    private String accessKey;

    @Value("${aliyun.key.secret}")
    private String secretKey;

    @Value("${aliyun.bucket.share.name}")
    private String shareBucketName;

    @Value("${aliyun.bucket.share.endpoint}")
    private String shareBucketUrl;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private OSSClient ossClient;

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


    /**
     *  消费发帖事件
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage (ConsumerRecord record) {

        // 空值处理
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        // 格式处理
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        // 再做一层检查
        if (event.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost post = postService.getDiscussPost(event.getEntityId());
            elasticService.saveDiscussPost(post);
        }

    }

    /**
     *  消费删帖事件
     */
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage (ConsumerRecord record) {

        // 空值处理
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        // 格式处理
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        elasticService.deleteDiscussPost(event.getEntityId());
    }

    /**
     *  消费分享图片事件
     */
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareImagesMessage (ConsumerRecord record) {

        if (record == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        // 处理事件
        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " "
                + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

        // 直接传图会有问题
        // 因为runtime方法和主线程是异步的，生成长图耗时，不会等待长图执行完成再执行
        // 所以在传图时可能图片还没有生成

        // 启动定时器，监视该图片，每隔半秒识别长图是否生成，一旦生成，上传至阿里云
        UploadTask uploadTask = new UploadTask(fileName, suffix);
        // 传完一定要停止该定时线程
        // 任务的返回值，封装了任务的状态，能够用来停止线程任务
        Future future = taskScheduler.scheduleAtFixedRate(uploadTask, 500);
        // 将future给与任务，保证可以停止
        uploadTask.setFuture(future);

    }

    class UploadTask implements Runnable{

        // 文件名称
        private String filename;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值，可以用来停止定时器
        private Future future;

        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;

        public void setFuture(Future future) {
            this.future = future;
        }

        // 构造器，要求传参
        public UploadTask(String filename, String suffix) {
            this.filename = filename;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            // 生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("执行时间过长，终止任务:" + filename);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3) {
                logger.error("上传次数过多，终止任务:" + filename);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + filename + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes, filename));
                OSS ossClient = null;
                try {
                    // 创建OSSClient实例。
                    ossClient = new OSSClientBuilder().build(shareBucketUrl, accessKey, secretKey);
                    // 创建PutObjectRequest对象。
                    String shareImgUrl = filename + suffix;
                    PutObjectRequest putObjectRequest =
                            new PutObjectRequest(shareBucketName, shareImgUrl, file);

                    ossClient.putObject(putObjectRequest);

                    future.cancel(true);
                } catch (OSSException e) {
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, filename));
                } finally {
                    ossClient.shutdown();
                }
            } else {
                logger.info("等待图片生成[" + filename + "].");
            }

        }
    }

}
