package com.mycode.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个实体的赞(用户、帖子、评论等等)
    // like:entity:entityType:entityId -> set(userId)
    // 谁给该实体点赞，就将该用户的id存储到集合当中，而不是简单的数字+1
    // 方便业务的拓展，如：想知道是谁给你点了赞
    public static String getEntityLikeKey (int entityType, int entityId) {

        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;

    }

}
