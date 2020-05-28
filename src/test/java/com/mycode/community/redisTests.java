package com.mycode.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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


    /**
     *  使用HyperLogLog
     */

    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog () {

        String redisKey = "test:hll:01";

        // 制造20万重复数据
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        for (int i = 1; i <= 100000; i++) {
            int rd = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, rd);
        }

        // 去重后的准确结果应该是 100000

        // 下面是HyperLogLog的去重结果
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
        System.out.println(size / 100000f - 1);

    }

    // HyperLogLog对数据进行合并
    // 统计了该月每一天的UV
    // 而需求可能是：我需要一周七天的UV，你需要将着七天的UV数据合在一起
    // HyperLogLog提供一个api能够自动的对UV进行合并

   // 将三组数据合并，再统计合并后的重复数据的总数
    @Test
    public void testHyperLogLogUnion () {

        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 50001; i <= 150000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 100000; i <= 200000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        // 合并，三种数据合并后再存储到HYPERLOGLOG中，所以需要准备一个新的key
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        // 合并后去重
        long size = redisTemplate.opsForHyperLogLog().size(unionKey);

        System.out.println(size);
    }


    /**
     *  bitmap的使用
     */

    // 统计一组数据的布尔值
    @Test
    public void testBitmap () {
        String redisKey = "test:bm:01";

        // bitmap不是一种新的数据类型，只是String的一种特殊的使用，按位存储

        // 记录 Boolean setBit(K var1:key , long var2:指定的位（索引）, boolean var4:布尔值)
        // 默认是0，就是说不处理就是false，只有为true时才进行处理，最终统计的往往是true的个数
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            // 当执行redis的execute方法时，底层会调用RedisCallback方法，会将redis的连接传入该方法
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

    }

    // 统计3组数据的布尔值，并对这3组数据做OR运算
    @Test
    public void testBitMapOperation () {

        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        // 运算之后redis会存入一个新的结果中
        String redisKey = "test:bm:or";

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // bitOp(声明何种运算符的接口, 指定运算结果存入的key， 对那几组数据进行运算):运算
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(),
                        redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
    }

}
