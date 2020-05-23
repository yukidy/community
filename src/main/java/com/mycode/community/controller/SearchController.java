package com.mycode.community.controller;

import com.mycode.community.entity.DiscussPost;
import com.mycode.community.entity.Page;
import com.mycode.community.entity.User;
import com.mycode.community.service.ElasticsearchService;
import com.mycode.community.service.LikeService;
import com.mycode.community.service.UserService;
import com.mycode.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // /search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search (String keyword, Page page, Model model) {

        // 搜索相关帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        List<Map<String, Object>> searchList = null;
        // 聚合数据
        if (searchResult != null) {
            searchList = new ArrayList<>();
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                // 帖子点赞数量
                map.put("likeCount", likeService.findLikeEntityCount(ENTITY_TYPE_POST, post.getId()));

                searchList.add(map);
            }
        }
        model.addAttribute("discussPosts", searchList);
        model.addAttribute("keyword", keyword);

        // 设置分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }


}
