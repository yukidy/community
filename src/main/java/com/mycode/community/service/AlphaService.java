package com.mycode.community.service;

import com.mycode.community.dao.AlphaDao;
import com.mycode.community.dao.DiscussPostMapper;
import com.mycode.community.dao.UserMapper;
import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.User;
import com.mycode.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype") //每次调用会产生不同的实例
public class AlphaService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    //实现依赖注入，service依赖于dao
    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private DiscussPostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    //构造器
    public AlphaService () {
        System.out.println("实例化AlphaService");
    }

    // 当容器实例化这个Bean后，在调用完构造器之后，该方法自动被调用
    // bean何时被初始化？ 在服务启动时被初始化，所以在服务器启动时，该方法就会被调用
    //要想让容器在适当的时候自动的调用这个方法，添加@PostConstruct注解
    @PostConstruct  //表明该方法会在构造器之后调用
    public void init () {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy  //表明在bean销毁之前去调用它
    public void destory () {
        //可在此释放某些资源
        System.out.println("销毁AlphaService");
    }

    public String find () {
        return alphaDao.select();
    }

    /**
     *  演示事务的管理方式
     *      声明式事务
     *      编程式事务
     */


    // 模拟事务-声明式事务
    // @Transactional 会选择一个默认的隔离方式
    // 想要手动指定隔离级别，添加相应参数：isolation

    // 配置事务是，很多时候都会连带着配置事务的传播机制：propagation
    // 什么叫传播机制？
    //     在写业务方法时，可能会调用另外一个组件的业务，业务方法A 调用 业务方法B，而这两个业务方法都可能加上@Transactional事务管理去管理事务，
    //     那么在A调B时，B的事务应该以谁为准，涉及到两个事务交叉在一起的问题，是以A的事务管理为准，还是B的，而事务的传播机制就是解决这种交叉的问题
    // 最常用的：
    // * REQUIRED: 支持当前事务（外部事物，调用我的那个事务，A.B，那么A就是外部事务，就是当前事务），如果不存在则创建新事务
    // * REQUIRES_NEW: 创建一个新的事务，并且暂停当前事务（外部事物），A.B,B无视A的事务，暂停掉A的事务，永远都创建一个新的事务，按照B自己的方式去执行
    // NESTED: 如果当前存在事务（外部事物）,则嵌套在该事务中执行（独立的提交和回滚），A.B，A有事务，B嵌套在A的事务里执行，有自己独立的提交和回滚
    //         如果外部事务不存在，则和REQUIRED一样。
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1 () {
        // 新增用户
        User user = new User();
        user.setUsername("唐晓东xxx");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/177t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("community project");
        discussPost.setContent("this is my first community project : 哈哈");
        discussPost.setCreateTime(new Date());
        postMapper.insertDiscussPost(discussPost);

        // 模拟造错-将abc转为整数
        Integer.valueOf("abc");

        return "ok";
    }

    // 使用编程式事务，需要用到该类
    @Autowired
    private TransactionTemplate transactionTemplate;
    // 模拟事务-编程式事务
    public Object save2 () {

        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("唐晓东yyy");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://images.nowcoder.com/head/188t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("你好");
                discussPost.setContent("this is a  community project : 哈哈");
                discussPost.setCreateTime(new Date());
                postMapper.insertDiscussPost(discussPost);

                // 模拟造错-将abc转为整数
                Integer.valueOf("abc");
                return "ok";
            }
        });

    }

    // 让该方法在多线程的环境下，被异步的调用
    // 就是说，启用一个线程去调用这个方法，这个线程和主线程是并发执行的、异步执行的
    @Async
    public void execute1 () {
        // 在测试方法中调用了该方法，见ThreadPoolTests
        logger.debug("execute1");
    }

    // 让该方法在定时任务的多线程的环境下，被异步的调用
    // @Scheduled(initialDelay = 10000, fixedRate = 1000)
    public void execute2 () {
        // 在测试方法中调用了该方法，见ThreadPoolTests
        logger.debug("execute2");
    }
}
