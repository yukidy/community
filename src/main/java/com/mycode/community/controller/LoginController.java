package com.mycode.community.controller;

import com.google.code.kaptcha.Producer;
import com.mycode.community.entity.User;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mycode.community.util.CommunityConstant.*;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProduce;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *  注册页面
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage () {
        return "/site/register";
    }

    /**
     *  登录页面
     *      打开登录页面，目的是给浏览器返回一个html
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage () {
        return "/site/login";
    }

    /**
     *  用户注册
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register (Model model, User user) {

        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            //注册成功,返回一个操作成功的界面
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            //注册失败，证明有错误信息
            //这边不管是否都有，都添加进来
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }

    }

    /**
     * 邮件激活
     * @param model
     * @param userId
     * @param code
     * @return "/site/operate-result"
     */
    //自定义域名格式: http://localhost:8080/community/activation/101(userId)/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation (Model model,
                              @PathVariable("userId") int userId, @PathVariable("code") String code) {

        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，您的账号已经被激活！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您的激活码存在错误！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";

    }

    /**
     * 生成验证码：
     *      通过登录页面路径后添加图片路径，返回给该方法，通过图片路径再返回图片给登录页面
     *
     *  返回的是一种特殊的类型：一张图片，返回值用void，因为需要我们通过response对象手动的向浏览器输出
     */
    /**
     * 重构：将存储在session中的验证码替换成redis
     * @param response
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha (HttpServletResponse response/*, HttpSession session*/) {

        // 生成验证码
        String text = kaptchaProduce.createText();
        BufferedImage image = kaptchaProduce.createImage(text);

        // 存入session
        //session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // 将验证码存入redis,60秒后过期
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);


        // 将验证码图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("加载验证码失败:" + e.getMessage());
        }

    }


    /**
     * 登录操作
     *      这里发现，请求路径/login与进入登录页面时的请求路径一样，可以这样吗？
     *          可以，只要请求方法不同，如进入页面是GET方法，而登录操作时POST方法，即可
     */
    /**
     * 重构验证码
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login (String username, String password, String code, boolean isrememberme,
                         Model model, HttpServletResponse response,/*, HttpSession session*/
                         @CookieValue(name = "kaptchaOwner", required = false, defaultValue = "") String kaptchaOwner) {
                        //当参数不是普通的参数，如实体User，那么spring mvc会自动将实体装入model里
                        //在页面中即可直接直接通过model获取这些参数
                        //  但如果参数是普通类型参数，字符串，基本类型这些，则spring mvc默认是不会加入model中的
                        //  此时有两种方法：1、将这些参数手动装入model中
                        //                 2、这些参数是存在于request对象当中的，当程序执行到html页面时，请求是还没有被销毁的
                        //                      所以页面可以通过request对象来取值，如${param.username}
        // 检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");

        String kaptcha = null;

        // 判断用户的验证码随机字符串是否存在
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        } else {
            model.addAttribute("codeMsg", "验证码失效!");
            return "/site/login";
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号，密码
        int expiredSeconds = isrememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) { //map中是否包含ticket
            //验证成功
            //将ticket传给cookie
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            //设置生效范围，不要写死，利用配置类
            cookie.setPath(contextPath);
            // setMaxAge单位为秒
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";  //重定向到首页
        } else {
            //验证失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }

    }

    /**
     * 退出登录
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout (@CookieValue("ticket") String ticket) {
        userService.logout(ticket);

        // 清理securityContext
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }


}
