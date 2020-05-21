package com.mycode.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {

    // 主题（事件的类型）
    private String topic;
    // 事件触发的人
    private int userId;
    // 事件的实体类型
    private int entityType;
    // 实体类型的具体id
    private int entityId;
    // 实体的作者
    private int entityUserId;
    // 除此之外，事件对象应具有通用性，将来项目可能会产生其他业务，其他事件
    // 在处理这些特殊的事件时，可能会需要新增特殊的数据，但是目前无法预判需要哪些数据，哪些字段属性

    // 使用map。让其他所有额外的数据都存储到map中，让他具有一定的拓展性
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    // 改造, set方法后返回当前对象 event ，好处：使用set方法时可以连续调用set
    // set().set().set() - 灵活
    // 使用有参构造器也可以，但是参数较多，比较麻烦，而且不灵活
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

//    public void setData(Map<String, Object> data) {
//        this.data = data;
//    }

    // 希望外界调用时，传过来的不是整个map，而是分为key value
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
