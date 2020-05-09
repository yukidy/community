package com.mycode.community.service;

import com.mycode.community.dao.UserMapper;
import com.mycode.community.entity.User;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.mycode.community.util.CommunityConstant.*;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    // 注入值用@Value,自定义域名
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById (int id) {
        return userMapper.selectById(id);
    }

    // 返回的一般是：密码不为空、账号不为空等等，可以用一个map将这些信息装起来
    public Map<String, Object> register (User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            // 手动抛出异常
            // 受查异常（必须捕获，否则编译不通过）和非受查异常
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 对象不为null，属性存在问题
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号（传入的参数取数据库查询，看是否存在问题）
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已存在！");
            return map;
        }

        /// 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));  // 生成随机字符串(用于密码加密)
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt())); // 密码加密
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        // 用于创建格式化的字符串以及连接多个字符串对象,类似C语言的sprintf
        // 生成随机整数替换占位符%d
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        /// 用户邮件激活
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 自定义激活域名: http://localhost:8080/community/activation/101(userId)/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 邮件激活
     * @return 激活状态
     */
    public int activation (int userId, String code) {

        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {    // 是否被激活
            // -已被激活，视为重复激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {  // 未被激活，是否激活码一致
            // -一致
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAIL;
        }

    }
}