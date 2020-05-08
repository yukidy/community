package com.mycode.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮箱的客户端：它将访问邮箱的功能委托了给新浪来做，相当于代替了应用的客户端
 */
@Component  //表示需要spring容器去管理，是一个通用的bean，在哪个层次都可以用
public class MailClient {

    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    //核心组件JavaMailSender，注入到spring容器当中
    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 发送邮箱需要的几个条件1、发送的邮箱的是谁， 2、接收邮箱的是谁，3、发送邮件的标题和内容是什么
     */

    //服务器发邮箱，发送人是固定的，就是配置当中的,注入发件人username
    @Value("${spring.mail.username}")
    private String from;

    //封装一个公有的方法，能够被外界所调用(发送到哪to，邮件的标题subject，邮件的内容content)
    public void sendMail (String to, String subject, String content) {

        try {
            //构建MimeMessage
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            //MimeMessage帮助类，帮助mimeMailMessage构建更加详细的内容
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMailMessage);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            //表示邮件的内容，html:true表示,若是不加该参数，默认表示该内容为普通文本
            //若加了该参数，表示支持html格式的邮件
            messageHelper.setText(content, true);
            javaMailSender.send(messageHelper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("send email error: " + e.getMessage());
        }
    }
}
