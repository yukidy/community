package com.mycode.community.testsCase;

import com.mycode.community.CommunityApplication;
import com.mycode.community.entity.DiscussPost;
import com.mycode.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService postService;

    private DiscussPost data;

    /**
     *  在类加载之前执行，所以该方法需要是静态
     *      只调用一次
     */
    @BeforeClass
    public static void beforeClass () {
        System.out.println("before class.");
    }

    /**
     *  在类销毁之后执行，所以该方法需要是静态
     *      只调用一次
     */
    @AfterClass
    public static void afterClass () {
        System.out.println("after class.");
    }

    /**
     *  在所有测试方法被调用前调用
     *      可被调用多次
     */
    @Before
    public void before () {
        System.out.println("before method.");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("美国防部请求国民警卫队支援华盛顿 遭多位州长拒绝");
        data.setContent("海外网6月3日电美国黑人男子弗洛伊德因警察暴力执法死亡后，抗议示威在美国多地持续发酵，首都华盛顿的白宫附近也出现了示威活动，特勤局等执法部门则与抗议者多次发生冲突。美国媒体2日报道称，美国国防部长马克·埃斯珀已经向多个州发出请求，期待当地的国民警卫队能赶赴华盛顿保障安全。然而，目前已经有至少4位州长拒绝了其请求。\n" +
                "\n" +
                "据美国有线电视新闻网（CNN）报道，来自弗吉尼亚州、纽约州、宾夕法尼亚州和特拉华州的4位民主党州长均拒绝派遣国民警卫队至华盛顿。“我可以确定，我们本来期待纽约州国民警卫队能够在昨晚到达华盛顿，但是这一许可被州长撤回了。”国防部发言人克里斯·米切尔在2日表示。\n" +
                "\n" +
                "纽约州州长安德鲁·科莫则在简报会上称，纽约州的国民警卫队正在专心应对当地的情况。“我不知道他们（国民警卫队）收到了什么请求，但我可以这样告诉你，我不会在此时把任何国民警卫队派出纽约州，因为我们需要他们应对可能出现的问题。”\n" +
                "\n" +
                "另有一位国防部官员向CNN透露，特拉华州原本也会派出支援，但部队却被临时调去了别处。特拉华州州长约翰·卡尼的办公室已经确认收到支援请求，但是考虑到当地的情况而决定拒绝。办公室方面称，拒绝支援也与总统特朗普的态度和行为有关。“说实话，白宫方面的说辞很可能造成不必要的混乱。因此，州长并不想让国民警卫队前去支援。特拉华州目前不会向华盛顿派部队。”州长的副幕僚长乔纳森·斯塔基说道。\n" +
                "\n" +
                "在弗吉尼亚州，一位了解相关信息的官员称，州长拉尔夫·诺瑟姆在与华盛顿市长穆里尔·鲍泽进行交流后拒绝了国防部的支援请求。鲍泽表示，他们并没有请求任何额外支援。而在2日早晨的CNN节目中，鲍泽再次说道，其办公室与请求支援没有任何关系。此外，宾夕法尼亚州官员也已经透露，出于对州内情况的担忧，他们不会派国民警卫队到华盛顿。\n" +
                "\n" +
                "1日，特朗普曾表示会考虑出动军队镇压骚乱，还督促各州加大应对抗议示威的力度。在此之后不久，有目击者称，执法部门对白宫附近的抗议者发射了橡胶子弹，现场则烟雾弥漫。然而，国防部方面在2日表示了否认，称国民警卫队没有发射催泪弹和橡胶子弹。\n" +
                "\n" +
                "报道指出，国民警卫队成员在从事其他工作的同时会接受军事训练，一般情况下被部署在各州内部。国民警卫队可以在州长的指挥下进行执法行动，而军队只能在总统动用《叛乱法》时才能开展类似行动。（海外网 赵健行）");
        data.setCreateTime(new Date());
        data.setScore(Math.random() * 2000);
        postService.addDiscussPost(data);
    }

    /**
     *  在所有测试方法被调用后调用
     *      可被调用多次
     */
    @After
    public void after () {
        System.out.println("after method.");

        // 删除测试数据
        postService.setDiscussPostStatus(data.getId(), 2);
    }

    @Test
    public void test1 () {
        System.out.println("test1");
    }

    @Test
    public void test2 () {
        System.out.println("test2");
    }

    @Test
    public void testFindById () {

        DiscussPost discussPost = postService.getDiscussPost(data.getId());
        // 判断查询到的结果和初始化的数据是否一致
        // 判断非空
        Assert.assertNotNull(discussPost);
        // 判断内容是否相等
        Assert.assertEquals(data.getTitle(), discussPost.getTitle());
        Assert.assertEquals(data.getContent(), discussPost.getContent());
        // 省略...
    }

    /**
     *  测试更新分数的方法是否正确
     */
    @Test
    public void testUpdateScore () {
        int rows = postService.setDiscussPostScore(data.getId(), 2000.00);
        Assert.assertEquals(1, rows);

        DiscussPost discussPost = postService.getDiscussPost(data.getId());
        // 判断两个double，两位小数，需要有第三个参数delta
        // 因为计算机只有整数，是没有小数的，小数其实是一个近似的值，底层是浮点表示法表示小数
        // 因此，需要给定精度，表示这是两位小数，判断两位小数想不想等
        Assert.assertEquals(2000.00, discussPost.getScore(), 2);
    }

}
