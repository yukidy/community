package com.mycode.community.util;

/**
 * 常量
 * 目的:复用
 */
public interface CommunityConstant {

    /**
     *  激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     *  激活失败
     */
    int ACTIVATION_FAIL = 2;

    /**
     *  默认状态的登录凭证的超时时间
     *  time: 12 hours
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;


    /**
     *  记住状态的登录凭证的超时时间
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

    /**
     *  实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     *  主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     *  主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     *  主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     *  主题：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     *  主题：删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     *  主题：分享
     */
    String TOPIC_SHARE = "share";

    /**
     *  系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     *  权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     *  权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     *  权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";

    /**
     *  帖子type：普通
     */
    int POST_TYPE_NORMAL = 0;

    /**
     *  帖子type：置顶
     */
    int POST_TYPE_TOP = 1;

    /**
     *  帖子status:正常
     */
    int POST_STATUS_NORMAL = 0;

    /**
     *  帖子status:加精
     */
    int POST_STATUS_WONDERFUL = 1;

    /**
     *  帖子status:拉黑
     */
    int POST_STATUS_DELETE = 2;

}
