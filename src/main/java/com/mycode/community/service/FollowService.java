package com.mycode.community.service;

import com.mycode.community.entity.User;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    // 关注
    public void follow (int userId, int entityType, int entityId) {

        // 关注时需要存储两份数据，一个是当前作出关注操作的用户的关注列表，一个是对方的粉丝列表
        // 需要保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });

    }

    // 取消关注
    public void unfollow (int userId, int entityType, int entityId) {

        // 关注时需要存储两份数据，一个是当前作出关注操作的用户的关注列表，一个是对方的粉丝列表
        // 需要保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });

    }

    // 查询用户关注实体的数量
    public long findFolloweeCount (int userId, int entityType) {

        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);

    }

    // 查询实体的粉丝数量
    public long findFollowerCount (int entityType, int entityId) {

        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);

    }

    // 查询当前用户是否关注该实体
    public boolean hasFollowed (int userId, int entityType, int entityId) {

        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null ? true : false;

    }

    // 查询某用户关注的人,支持分页
    public List<Map<String, Object>> findFollowees (int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // range()是截取索引，与mysql的分页不同 如：offset = 0， limit = 10,索引取10个值
        // 0~9 offset~offfset + limit - 1
        // 在调用redis的zset的range方法，查到一个范围内的数据，它会做一个排序，排序后返回的是一个set集合
        // 默认情况下，jdk的set集合是无序的，但是redis的返回的Set的集合的实现类是内置的，是redis自己实现的，是一个有序的set集合
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit -1);
        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> followeeList = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double sorce = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(sorce.longValue()));
            followeeList.add(map);
        }

        return followeeList;
    }


    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers (int userId, int offset, int limit) {
        // 获得的是当前用户的粉丝
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit -1);
        if (followerIds == null) {
            return null;
        }

        List<Map<String, Object>> followerList = new ArrayList<>();
        for (Integer targetId : followerIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            followerList.add(map);
        }
        return followerList;

    }


}
