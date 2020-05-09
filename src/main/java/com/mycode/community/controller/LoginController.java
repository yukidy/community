package com.mycode.community.controller;

import com.mycode.community.entity.User;
import com.mycode.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import static com.mycode.community.util.CommunityConstant.ACTIVATION_REPEAT;
import static com.mycode.community.util.CommunityConstant.ACTIVATION_SUCCESS;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     *  注册页面
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage () {
        return "/site/register";
    }

    /**
     *  登录页面
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
}
