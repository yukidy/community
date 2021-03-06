package com.mycode.community.controller;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.Page;
import com.mycode.community.entity.User;
import com.mycode.community.service.DiscussPostService;
import com.mycode.community.service.LikeService;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Controller下的访问路径其实是可以为空的，直接访问方法路径就可以
//不需要@RequestMapping
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage (Model model, Page page,
                                @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        //方法调用前，springMVC会自动实例化Model和Page，并将Page注入Model，
        //所以，在thymeleaf中可以直接访问Page对象中的数据

        if (orderMode != 0 && orderMode != 1) {
            orderMode = 0;
        }

        /**
         *  首页优化-数据过多时，查询会大大降低性能
         *      若是数据大于100页，只显示前100页内容
         */

        //查询总页数,存入page
        page.setRows(discussPostService.findDiscussPostRows(0));

        //设置路径，做分页
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
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

                long likeCount = likeService.findLikeEntityCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
            model.addAttribute("discussPosts", discussPosts);
            model.addAttribute("orderMode", orderMode);
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

    /**
     *  没有权限访问某功能时，security授权-普通请求返回页面
     */
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage () {
        return "/error/404";
    }

}
