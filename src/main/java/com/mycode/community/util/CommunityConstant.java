package com.mycode.community.util;

/**
 * 常量
 * 目的:复用
 */
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAIL = 2;

    /**
     * 默认状态的登录凭证的超时时间
     *  time: 12 hours
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;


    /**
     * 记住状态的登录凭证的超时时间
     *  time: 30 days
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    /**
     *  实体类型：对帖子评论
     */
    int ENTITY_TYPE_POST = 1;

    /**
     *  实体类型：对评论评论
     */
    int ENTITY_TYPE_COMMENT = 2;


}
