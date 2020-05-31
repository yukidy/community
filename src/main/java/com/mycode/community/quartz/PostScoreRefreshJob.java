package com.mycode.community.quartz;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.service.ElasticsearchService;
import com.mycode.community.service.LikeService;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService postService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // ACG网创始纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-04-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化ACG网创始纪元失败！", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getPostScoreRefreshKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕：" + operations.size());


    }

    // 刷新帖子分数
    private void refresh(int postId) {

        DiscussPost post = postService.getDiscussPost(postId);
        if (post == null) {
            logger.error("该帖子不存在： id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findLikeEntityCount(ENTITY_TYPE_POST, post.getId());

        // 计算(权重)
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        // 分数 = 帖子权重 + 距离天数
        // Math.max(w, 1) 返回w和1两个数中的最大值
        double score = Math.log10(Math.max(w, 1)
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24));

        // 更新帖子分数
        postService.setDiscussPostScore(postId, score);

        // 更新elasticSearch数据
        post.setScore(score); // 避免再查一次更新后的数据库
        elasticsearchService.saveDiscussPost(post);

    }
}
