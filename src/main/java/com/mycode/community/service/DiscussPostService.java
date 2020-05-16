package com.mycode.community.service;

import com.mycode.community.dao.DiscussPostMapper;
import com.mycode.community.entity.DiscussPost;
import com.mycode.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 下面两个方法查询到的userId,在实际页面中应该是显示用户的名称而不是用户的id
     *  办法1：在写sql时关联用户id，把用户数据也查询
     *  办法2：用得到的userId单独的去查询到用户数据，再合并两种数据得到可用的列表
     *      选择方法2，这种方式更加的直观，而且后面使用到redis，缓存数据时，更加方便，性能更高，代码更加直观
     */

    public List<DiscussPost> findDiscussPosts (int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows (int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 发布帖子
     * @param post 帖子
     * @return
     */
    public int addDiscussPost (DiscussPost post) {

        //空值处理
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 1、敏感词过滤， 需过滤的功能 - title/content
        // 2、去标签 - spring工具有该功能
        // 把文字当中的标签也过滤掉，如下，不过滤的话，可能显示在网页上时，可能会表现出标签的特点
        // 也可能会存在对网页的一些破坏性，对网页有影响
        //  <script>xxx</script>

        // 转义html标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        // 过滤敏感词
        post.setTitle(sensitiveFilter.sensitivefilter(post.getTitle()));
        post.setContent(sensitiveFilter.sensitivefilter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 查找详情贴
     * @param id 帖子id
     * @return 帖子详情
     */
    public DiscussPost getDiscussPost (int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 更新帖子评论数
    public int updateCommentCount (int id, int count) {
        return discussPostMapper.updateCommentCount(id, count);
    }

}
