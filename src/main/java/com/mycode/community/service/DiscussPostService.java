package com.mycode.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mycode.community.dao.DiscussPostMapper;
import com.mycode.community.entity.DiscussPost;
import com.mycode.community.util.RedisKeyUtil;
import com.mycode.community.util.SensitiveFilter;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口： Cache (代表一个缓存)，
    // Cache的两个子接口：LoadingCache（同步缓存）（常用），AsyncLoadingCache（异步）
    // LoadingCache（同步缓存）: 多个线程同时访问该缓存中的同一份数据，缓存中没有该数据，他会让这些线程等待，从数据库中取数据后，排队返回数据
    // AsyncLoadingCache（异步）： 支持并发的同时取数据。

    // 帖子列表的缓存，需要初始化才能使用
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数的缓存,需要初始化才能使用
    private LoadingCache<Integer, Integer> postRowsCache;

    // ?缓存何时初始化
    // 在首次启动服务或者首次调用该service时，初始化一次即可。

    @PostConstruct
    public void init () {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                // 让参数配置生效
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    // CacheLoader接口的作用：当尝试读取缓存数据时，caffeine会尝试读取缓存中的数据，有就返回
                    // 如果没有，需要知道如何去查询这些数据，然后将数据装入缓存中，返回给用户
                    // 所以需要提供缓存一个查询数据库，并且得到数据存入缓存的方法
                    // load()方法就是能查询数据存入缓存的办法，缓存的数据来源
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {

                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("缓存Key错误！");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 此处可以使用二级缓存
                        // 二级缓存： redis -> mysql

                        // 先从redis缓存取数据进行初始化
                        String redisKey = RedisKeyUtil.getPostHotCacheKey(key);
                        if (redisTemplate.hasKey(redisKey) && limit == redisTemplate.opsForList().size(redisKey)) {
                            // redis缓存中取数据进行初始化
                            logger.debug("load post list from Redis.");
                            return redisTemplate.opsForList().range(redisKey, 0, limit - 1);
                        } else {
                            // redis中没有缓存的数据
                            // DB数据进行初始化
                            logger.debug("load post list from DB.");
                            List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, offset, limit, 1);

                            // 将数据库中的数据存储到redis中
                            redisTemplate.opsForList().rightPushAll(redisKey, list);
                            // 设置过期时间- 比本地缓存长
                            redisTemplate.expire(redisKey, 10, TimeUnit.MINUTES);

                            return list;
                        }

                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        // 初始化 该key永远是0
                        String redisKey = RedisKeyUtil.getPostRowsCacheKey(key);
                        if (redisTemplate.hasKey(redisKey)
                                && (int) redisTemplate.opsForValue().get(redisKey) >= 0) {
                                logger.debug("load post list from Redis.");
                                return (Integer) redisTemplate.opsForValue().get(redisKey);
                        } else { // redis缓存不存在，从数据库中取
                            int rows = discussPostMapper.selectDiscussPostRows(key);
                            if (rows > 1000) {
                                rows = 1000;
                            }
                            redisTemplate.opsForValue().set(redisKey, rows);
                            redisTemplate.expire(redisKey, 6, TimeUnit.MINUTES);

                            logger.debug("load post rows from DB.");
                            return rows;
                        }

                    }
                });

    }

    /**
     * 下面两个方法查询到的userId,在实际页面中应该是显示用户的名称而不是用户的id
     *  办法1：在写sql时关联用户id，把用户数据也查询
     *  办法2：用得到的userId单独的去查询到用户数据，再合并两种数据得到可用的列表
     *      选择方法2，这种方式更加的直观，而且后面使用到redis，缓存数据时，更加方便，性能更高，代码更加直观
     */

    public List<DiscussPost> findDiscussPosts (int userId, int offset, int limit, int orderMode) {
        // 热帖缓存
        if (userId == 0 && orderMode == 1) {
            // key: offset + ":" + limit
            return postListCache.get(offset + ":" + limit);
        }

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows (int userId) {
        // 总数缓存,这里让key永远都是0
        if (userId == 0) {
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);;

    }

    /**
     * 发布帖子
     * @param post 帖子
     * @return
     */
    public int addDiscussPost (DiscussPost post) {

        //空值处理
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 1、敏感词过滤， 需过滤的功能 - title/content
        // 2、去标签 - spring工具有该功能
        // 把文字当中的标签也过滤掉，如下，不过滤的话，可能显示在网页上时，可能会表现出标签的特点
        // 也可能会存在对网页的一些破坏性，对网页有影响
        //  <script>xxx</script>

        // 转义html标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        // 过滤敏感词
        post.setTitle(sensitiveFilter.sensitivefilter(post.getTitle()));
        post.setContent(sensitiveFilter.sensitivefilter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 查找详情贴
     * @param id 帖子id
     * @return 帖子详情
     */
    public DiscussPost getDiscussPost (int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 更新帖子评论数
    public int updateCommentCount (int id, int count) {
        return discussPostMapper.updateCommentCount(id, count);
    }


    /**
     * 更改帖子的类型
     * @param postId 帖子id
     * @param type 0-普通、1-置顶
     * @return
     */
    public int setDiscussPostType (int postId, int type) {
        return discussPostMapper.updateDiscussPostType(postId, type);
    }


    /**
     * 更改帖子的状态
     * @param postId 帖子id
     * @param status 0-正常、1-精华、2-拉黑
     * @return
     */
    public int setDiscussPostStatus (int postId, int status) {
        return discussPostMapper.updateDiscussPostStatus(postId, status);
    }

    /**
     * 修改帖子的分数
     * @param postId
     * @param score
     * @return
     */
    public int setDiscussPostScore (int postId, double score) {
        return discussPostMapper.updateScore(postId, score);
    }

    @Transactional
    public void insertBatchPosts (List<DiscussPost> list) {

        int groupSize = 1000;
        int groupNo = list.size() / 1000;
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        DiscussPostMapper mapper = sqlSession.getMapper(DiscussPostMapper.class);

        if (list.size() <= groupSize) {
            mapper.insertBatchDiscussPosts(list);
        } else {
            List<DiscussPost> subList = null;
            for (int i = 0; i < groupNo; i++) {
                subList = list.subList(0, groupSize);
                mapper.insertBatchDiscussPosts(subList);
                list.subList(0, groupSize).clear();
            }
            if (list.size() > 0) {
                mapper.insertBatchDiscussPosts(list);
            }
        }

        sqlSession.flushStatements();
    }
}
