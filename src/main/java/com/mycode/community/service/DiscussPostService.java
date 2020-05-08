package com.mycode.community.service;

import com.mycode.community.dao.DiscussPostMapper;
import com.mycode.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

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


}
