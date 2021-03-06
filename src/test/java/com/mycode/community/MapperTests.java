package com.mycode.community;

import com.mycode.community.dao.DiscussPostMapper;
import com.mycode.community.dao.LoginTickerMapper;
import com.mycode.community.dao.MessageMapper;
import com.mycode.community.dao.UserMapper;
import com.mycode.community.entity.*;
import com.mycode.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTickerMapper loginTickerMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelect () {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsert () {
        User user = new User();
        user.setUsername("唐晓东");
        user.setPassword("123456");
        user.setSalt("abcd");
        user.setEmail("nowcoder150@sina.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/100t.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
        System.out.println(userMapper.selectByName("唐晓东"));
    }

    @Test
    public void testUpdate () {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://images.nowcoder.com/head/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "654321");
        System.out.println(rows);
    }



    @Test
    public void testSelectPosts () {
        List<DiscussPost> dPMList = discussPostMapper.selectDiscussPosts(149, 0, 10, 0);
        for (DiscussPost post:dPMList) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket () {

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(153);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        // new Date()，获取当前系统时间
        // new Date(System.currentTimeMillis() + 1000 * 60 * 10)，获取当前时间的时间戳，比当前时间多10分钟
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTickerMapper.insertLoginTicket(loginTicket);

    }

    @Test
    public void testSelectLoginTicket () {

        LoginTicket loginTicket = loginTickerMapper.selectByTicket("b2b3d4954de94c9fb6fa1c83344fe9ba");
        System.out.println(loginTicket);

        loginTickerMapper.updateStatus("b2b3d4954de94c9fb6fa1c83344fe9ba", 1);
        loginTicket = loginTickerMapper.selectByTicket("b2b3d4954de94c9fb6fa1c83344fe9ba");
        System.out.println(loginTicket);

    }

    @Test
    public void testInsertDiscussPost () {

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(153);
        discussPost.setTitle("community project");
        discussPost.setContent("this is my first community project : 哈哈");
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setScore(0);
        System.out.println(discussPostMapper.insertDiscussPost(discussPost));

    }

    @Test
    public void testSelectDiscussPost () {

        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(155);
        System.out.println(discussPost);

    }

    /**
     * 测试message-mapper方法
     */
    @Test
    public void testSelectConversation () {

        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message:list) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        list =messageMapper.selectLetters("111_112", 0, 20);

        for (Message message:list) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectLetterCount("111_112"));

        System.out.println(messageMapper.selectLetterUnreadCount(131, "111_131"));

    }

}
