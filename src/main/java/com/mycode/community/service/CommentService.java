package com.mycode.community.service;

import com.mycode.community.dao.CommentMapper;
import com.mycode.community.entity.Comment;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Comment> findCommentByEntity (int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    public int findCountByEntity (int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     *  添加评论
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment (Comment comment) {

        // 空值处理
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 对页面传过来的实体，对内容进行一个过滤，包括Html标签、敏感词等进行处理
        // 标签过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 敏感词过滤
        comment.setContent(sensitiveFilter.sensitivefilter(comment.getContent()));

        // 添加评论
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论的数量
        // 但是要区分，评论可以评论给评论，可以评论给帖子，但是只有评论给帖子的评论才会被更新到帖子评论的数量中
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 对帖子的评论
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());

            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;

    }

    public Comment findCommentById (int id) {
        return commentMapper.selectCommentById(id);
    }

}
