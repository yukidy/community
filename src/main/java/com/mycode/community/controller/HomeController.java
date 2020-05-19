package com.mycode.community.controller;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.Page;
import com.mycode.community.entity.User;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Controller下的访问路径其实是可以为空的，直接访问方法路径就可以
//不需要@RequestMapping
@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage (Model model, Page page) {
        //方法调用前，springMVC会自动实例化Model和Page，并将Page注入Model，
        //所以，在thymeleaf中可以直接访问Page对象中的数据

        //查询总页数,存入page
        page.setRows(discussPostService.findDiscussPostRows(0));
        //设置路径，做分页
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        //该集合是一个能够封装DiscussPost对象和User对象的一个对象，再写一个类、或者一个数组、或者map都行
        //这里使用map
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                //map 的 key 和 value 放的都是对象的引用。
                //key 和 value 不能放基本数据类型，只能放对象的引用，如果你把基本数据类型放进去，也会自动装箱成包装类。
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId()); //? 考虑到redis的存在，这边使用单表多次查询
                map.put("user", user);
                discussPosts.add(map);
            }
            model.addAttribute("discussPosts", discussPosts);
        }
        return "/index";
    }

    /**
     * 服务器发生异常，统一处理，记录日志之后怎么办？
     *  由于是人为处理，这时我们需要手动的重定向到500页面
     * @return
     */
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage () {
        return "/error/500";
    }

}
