package com.mycode.community;

import com.mycode.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    //测试类中不像Spring mvc那样，这里需要主动的去调用模板引擎
    //注入模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail () {
        mailClient.sendMail("1298811823@qq.com", "Test01", "Welcome,");
    }

    @Test
    public void testHtmlMail () {
        //访问模板，需要给模板传参，利用Context对象，找thymeleaf下的Context对象
        Context context = new Context();
        context.setVariable("username", "sunday");

        //参数构建完之后，就调用模板引擎，生成动态网页
        String content = templateEngine.process("/mail/mailDemo", context);
        //将内容打印到控制台
        System.out.println(content);

        //发邮件
        mailClient.sendMail("1298811823@qq.com", "test02", content);

    }

}
