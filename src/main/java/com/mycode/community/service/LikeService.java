package com.mycode.community.service;

import com.mycode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like (int userId, int entityType, int entityId) {

        String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        BoundSetOperations operations = redisTemplate.boundSetOps(likeEntityKey);

        boolean ismembers = operations.isMember(userId);
        if (ismembers) {
            // 已存在
            operations.remove(userId);
        } else {
            // 不存在
            operations.add(userId);
        }

    }

    // 查询某实体点赞数量
    public Long findLikeEntityCount (int entityType, int entityId) {

        String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(likeEntityKey);

    }


    // 查询某人对某实体的点赞状态（0-未点; 1-已点）
    public int findLikeEntityStatus (int userId, int entityType, int entityId) {

        String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(likeEntityKey, userId) ? 1 : 0;

    }

}
