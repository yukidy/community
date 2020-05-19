package com.mycode.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class redisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings () {

        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    @Test
    public void testHashes () {

        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "张三");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));

    }

    @Test
    public void testLists () {

        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForList().index(redisKey, 1));

        System.out.println(redisTemplate.opsForList().rightPop(redisKey));

    }

    @Test
    public void testSets () {

        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "战三", "李四", "王五", "升六");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void sortSets () {

        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "唐三", 90);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 80);
        redisTemplate.opsForZSet().add(redisKey, "猪八戒", 70);
        redisTemplate.opsForZSet().add(redisKey, "吴金", 60);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 50);

        System.out.println(redisTemplate.opsForZSet().count(redisKey, 60, 80));
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "猪八戒"));
        // 由大到小倒叙取前三名
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
        // 由大到小倒叙取八戒的排名
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "猪八戒"));


    }

    @Test
    public void testKeys () {

        redisTemplate.delete("test:ids");
        System.out.println(redisTemplate.hasKey("test:ids"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);

        System.out.println(redisTemplate.getExpire("test:students"));

    }


    // 多次访问同一key，以绑定的方式存在,方便重复使用时减去繁琐的操作
    @Test
    public void testBoundOperations () {

        String redisKey = "test:count";

        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);

        operations.set(1);
        operations.increment();
        operations.increment();

        System.out.println(operations.get());

    }

    // 编程式事务
    @Test
    public void testTransactional () {

        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 在调用execute方法时，会传进redisOperations执行命令的对象，来管理事务

                String redisKey = "test:tx";

                // 启用事务
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, 1);
                redisOperations.opsForSet().add(redisKey, 2);
                redisOperations.opsForSet().add(redisKey, 3);

                System.out.println(redisOperations.opsForSet().members(redisKey));

                // 返回的数据会返回给execute方法，用对象接收
                // redisOperations.exec()提交事务
                return redisOperations.exec();
            }
        });

        System.out.println(obj);

    }

}
