package com.mycode.community.controller;

import com.mycode.community.annotation.LoginRequired;
import com.mycode.community.entity.User;
import com.mycode.community.service.FollowService;
import com.mycode.community.service.LikeService;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.CommunityUtil;
import com.mycode.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;


@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;  //域名

    @Value("${community.path.upload}")
    private String uploadPath;  //上传路径

    @Value("${server.servlet.context-path}")
    private String contextPath; //项目访问路径

    @Autowired
    private UserService userService; //更新图片

    @Autowired
    private HostHolder holder;  //更新的是当前用户的头像

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    // 进入设置页面
//    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage () {
        return "/site/setting";
    }

    // 处理上传的文件
//    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    // 这个方法接收的是页面上的一个文件类型的参数，spring MVC专门有一个MultipartFile类型来接收文件
    // 如果页面传了多个文件，可以用数组接收MultipartFile[]
    public String uploadHeader (MultipartFile headerImage, Model model) {

        // 空值处理
        if (headerImage == null) {
            model.addAttribute("errorMsg", "您还没有选择图片！");
            return "/site/setting";
        }

        // 上传文件-图片

        String filename = headerImage.getOriginalFilename();
        // 后缀：从最后一个点的索引开始，往后截取
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("errorMsg", "您的图片文件格式不正确!");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 存储路径: 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //  存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传图片失败:" + e.getMessage());
            throw new RuntimeException("上传图片异常，服务器发生错误!", e);
        }

        // 更新当前用户的头像路径(web访问路径)
        // 用户是无法通过uploadPath，就是说你的本地硬盘中访问到图片路径
        // 你应该更新的是该图片在你的服务器的访问路径
        // 如：http://localhost:8080/community/user/header/xxx.png
        User user = holder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeaderUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }

    // 获取头像
    // 这里的访问路径就要按照自定义访问路径的规定：http://localhost:8080/community/user/header/xxx.png
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    // 这个方法向浏览器响应的不是字符串，也不是网页，而是一个图片，二进制的数据，较特殊，需要通过流手动的想浏览器输出
    public void getHeaderImage (@PathVariable("filename") String filename, HttpServletResponse response) {

        // 服务器存放路径
        filename = uploadPath + "/" + filename;
        // 后缀
        String suffix = filename.substring(filename.lastIndexOf("."));

        // 声明向浏览器输出的格式
        response.setContentType("image/" + suffix);
        // 响应图片
        try (   // try里加小括号，在括号里的对象，会自动关闭流，相当于在finally里添加close()方法
                // os是response所管理的，不需要操作也会自动帮你关闭
                // 但是输入流FileInputStream是自己创建的，所以需要手动关闭

                OutputStream os = response.getOutputStream();  //获取字节流
                // 创建一个输入流，读文件，一边读取文件信息，才能输出文件信息
                FileInputStream fis = new FileInputStream(filename);
        ) {
            // 输出流，不要一个字节一个字节输出，可以建立缓冲区，一批一批输出，效率更高
            // 比如一次输出1024个字节
            byte[] buffer = new byte[1024];
            int b = 0;  // 游标
            while ((b = fis.read(buffer)) != -1) {  //每次输入流读取到的数据复制给b
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }

    }


    // 修改密码
//    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword (String oldPassword, String newPassword, Model model) {

        // 空值处理
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            model.addAttribute("passwordMsg", "密码不能为空！");
            return "/site/setting";
        }

        //  密码检测
        User user = holder.getUser();
        String passwordMD = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(passwordMD)) {
            model.addAttribute("passwordMsg", "密码不正确!");
            return "/site/setting";
        }

        // 长度检测

        // 更新密码
        passwordMD = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), passwordMD);
        model.addAttribute("msg", "您的密码修改成功！");
        model.addAttribute("target", "/index");
        return "/site/operate-result";

    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage (@PathVariable("userId") int userId, Model model) {

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前登录用户是否关注TA
        boolean hasFollowed = false;
        // 判断用户有没有登录
        if (holder.getUser() != null) {
            hasFollowed = followService.hasFollowed(holder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        // 没有登录默认为false
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}
