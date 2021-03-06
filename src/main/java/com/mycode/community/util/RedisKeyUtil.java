package com.mycode.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    // a关注b，a是b的追随者：follower; b是a的关注目标：followee
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";

    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";

    // 用户
    private static final String PREFIX_USER = "user";

    // UV(unique visitor) 独立访客
    private static final String PREFIX_UV = "uv";

    // DAU(daily active user) 日活跃用户
    private static final String PREFIX_DAU = "dau";

    // 需要刷新分数的帖子列表
    private static final String PREFIX_SCORE = "post:refresh";

    // 热帖redis缓存
    private static final String PREFIX_HOT_POST_CACHE = "post:hot:cache";

    // 帖子总行数:redis缓存
    private static final String PREFIX_ROWS_POST_CACHE = "post:rows:cache";

    // 某个实体的赞(帖子、评论等等)
    // like:entity:entityType:entityId -> set(userId)
    // 谁给该实体点赞，就将该用户的id存储到集合当中，而不是简单的数字+1
    // 方便业务的拓展，如：想知道是谁给你点了赞
    public static String getEntityLikeKey (int entityType, int entityId) {

        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;

    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey (int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId, now)
    // 某个用户:userId -> 关注的实体entityType -> 实体的具体信息entityId -> 关注的时间now
    public static String getFolloweeKey (int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个用户拥有的粉丝
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey (int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 验证码的key
    // 不通过session存储验证码的情况下，验证码如何在用户未登录的情况下反馈给用户？
    // 在用户未登录的情况下，用户的id是未知的，问题是我们还需要识别用户是谁，
    // 可以在用户访问登录页面时，发一个凭证（一个随机生成的字符串）过去，然后客户端存储到cookie中，
    // 以该字符串来标识此用户，临时的（设置比较短的过期时间即可）
    // String owner：用户临时的凭证
    public static String getKaptchaKey (String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证的key
    public static String getTicketKey (String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey (int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUvKey (String date) { // date:年月日
        return PREFIX_UV + SPLIT + date;
    }

    // 合并UV，区间UV - key
    public static String getUvKey (String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日DAU
    public static String getDauKey (String date) { // date:年月日
        return PREFIX_DAU + SPLIT + date;
    }

    // 合并DAU，区间DAU - key
    public static String getDauKey (String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 定时刷新分数的帖子列表的redis key
    public static String getPostScoreRefreshKey () {
        return PREFIX_SCORE + SPLIT + "score";
    }

    // 返回热帖redis缓存key  post:hot:cache:${offset}:${limit}
    public static String getPostHotCacheKey (String cacheKey) {
        return PREFIX_HOT_POST_CACHE + SPLIT + cacheKey;
    }

    // 返回帖子总行数的redis缓存key  post:rows:cache:0
    public static String getPostRowsCacheKey (int key) {
        return PREFIX_ROWS_POST_CACHE + SPLIT + key;
    }


}
