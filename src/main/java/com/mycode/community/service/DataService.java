package com.mycode.community.service;

import com.mycode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    // 将指定的IP计入UV
    public void recordUV (String ip) {
        String redisKey = RedisKeyUtil.getUvKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }


    // 统计指定日期范围内的UV
    public Long calculateUnionUV (Date startDate, Date endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        // 如果开始日期不晚于终止日期，进入循环
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getUvKey(dateFormat.format(calendar.getTime()));
            keyList.add(key);
            // startDate日期加1
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据
        String unionKey = RedisKeyUtil.getUvKey(dateFormat.format(startDate), dateFormat.format(endDate));
        redisTemplate.opsForHyperLogLog().union(unionKey, keyList.toArray());

        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }


    // 将指定的用户计入DAU
    public void recordDAU (int userId) {
        String redisKey = RedisKeyUtil.getDauKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }


    // 统计指定日期范围内的DAU
    public long calculateUnionDAU (Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        // 如果开始日期不晚于终止日期，进入循环
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getDauKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 合并数据,进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDauKey(dateFormat.format(startDate), dateFormat.format(endDate));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }

}
