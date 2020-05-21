package com.mycode.community.service;

import com.mycode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like (int userId, int entityType, int entityId, int entityUserId) {

//        String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        BoundSetOperations operations = redisTemplate.boundSetOps(likeEntityKey);
//
//        boolean ismembers = operations.isMember(userId);
//        if (ismembers) {
//            // 已存在
//            operations.remove(userId);
//        } else {
//            // 不存在
//            operations.add(userId);
//        }

        // 重构
        // 对帖子/评论的点赞
        // + 用户的点赞功能，有两次对redis数据的操作，应该保证事务性

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String likeEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // entityUserId:发布内容者的id，通过页面传进来
                // entityId可以查找到相关发布者Id,但是需要从数据库中查找，不走这一步
                String likeUserKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean ismembers = operations.opsForSet().isMember(likeEntityKey, userId);

                // 开启事务
                operations.multi();

                if (ismembers) {
                    // 已存在
                    operations.opsForSet().remove(likeEntityKey, userId);
                    operations.opsForValue().decrement(likeUserKey);
                } else {
                    // 不存在
                    operations.opsForSet().add(likeEntityKey, userId);
                    operations.opsForValue().increment(likeUserKey);
                }

                // 提交事务
                return operations.exec();
            }
        });


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

    // 查询某个用户获得的点赞数量
    public int findUserLikeCount (int userId) {

        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();

    }

}
