package com.mycode.community.controller;

import com.google.code.kaptcha.Producer;
import com.mycode.community.entity.User;
import com.mycode.community.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static com.mycode.community.util.CommunityConstant.ACTIVATION_REPEAT;
import static com.mycode.community.util.CommunityConstant.ACTIVATION_SUCCESS;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProduce;

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
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha (HttpServletResponse response, HttpSession session) {

        // 生成验证码
        String text = kaptchaProduce.createText();
        BufferedImage image = kaptchaProduce.createImage(text);

        // 存入session
        session.setAttribute("kaptcha", text);

        // 将验证码图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("加载验证码失败:" + e.getMessage());
        }

    }

}
