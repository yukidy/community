package com.mycode.community;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

    /**
     *  插入大量数据，让mysql性能降低,贴近现实，与有缓存之后形成对比
     */
    @Test
    public void initDataTest () {
        // 插入大量数据，将AOP日志处理关闭

        // 单条数据插入-经过测试、龟速
        // insert into discuss_post() values(?, ?, ?, ?, ?);
        // 程序不断的与数据库连接、断开、连接、断开
        for (int i = 0; i < 1000000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网求职暖春计划");
            post.setContent("今年的就业形势，确实不容乐观。过了个年，仿佛跳水一般，整个讨论区哀鸿遍野！19届真的没人要了吗？！18届被优化真的没有出路了吗？！大家的“哀嚎”与“悲惨遭遇”牵动了每日潜伏于讨论区的牛客小哥哥小姐姐们的心，于是牛客决定：是时候为大家做点什么了！为了帮助大家度过“寒冬”，牛客网特别联合60+家企业，开启互联网求职暖春计划，面向18届&19届，拯救0 offer！");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            postService.addDiscussPost(post);
        }

    }

    /**
     *  批量插入
     *      insert into discuss_post() values(??)(??)(??)(??)..;
     *    可以避免程序和数据库建立多次连接，从而增加服务器负荷。
     *    提升效率
     *    100万条大约在 1分半到2分钟左右
     *
     */
    @Test
    public void insertBatchDataTest () {
        List<DiscussPost> list = new ArrayList<>();
        DiscussPost post = new DiscussPost();
        post.setUserId(111);
        post.setTitle("特朗普发文自夸：华盛顿没出状况，得谢谢特朗普总统！");
        post.setContent("美东时间2日上午，美国总统特朗普发布推特称，“华盛顿特区昨晚没出什么状况。许多人被抓了。所" +
                "有各方都干得漂亮。（这是）压倒性的力量。控制住了。同样，明尼阿波利斯也做得很棒（这得谢谢特朗普总统！）早前一天，面对席卷全美的持续抗议活动，特朗普发表讲话称，他将立即采取总统行动“制止暴力，恢复美国的安全与保障”，并动员联邦资源制止暴乱和抢劫行为。他称自己将援引1807年的《叛乱法案》，动员全国各地的军队，“我将部署美国军队，并迅速为他们解决问题。”");
        post.setCreateTime(new Date());
        post.setScore(Math.random() * 2000);
        for (int i = 0; i < 1000000; i++) {
            list.add(post);
        }
        System.out.println(list.size());
        postService.insertBatchPosts(list);

    }

    /**
     * 测试缓存
     */
    @Test
    public void testCache () {
        // 第1次取，从数据库中
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        // 第2、3、4次，从本地缓存中
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        // 按时间排，从数据库中取
        System.out.println(postService.findDiscussPosts(0, 0, 10, 0));
    }


    /**
     *  进行压力测试
     *      1、不用缓存进行压力测试-删掉service相关代码
     */


}
