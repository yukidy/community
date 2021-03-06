package com.mycode.community.dao;

import com.mycode.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 根据实体类型查询评论：帖子的评论，评论的评论等类型
    List<Comment> selectCommentByEntity (int entityType, int entityId, int offset, int limit);

    // 查询评论条目数
    int selectCountByEntity (int entityType, int entityId);

    // 添加评论
    int insertComment (Comment comment);

    // 查询一条评论
    Comment selectCommentById (int id);

    // 查询某用户对帖子的评论数（不去重）
    int selectUserPostCommentCount (int userId, int entityType);

    // 查询用户对帖子的评论
    List<Comment> selectUserPostCommentList (int userId, int entityType);
}
