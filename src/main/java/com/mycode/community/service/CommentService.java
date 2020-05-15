package com.mycode.community.service;

import com.mycode.community.dao.CommentMapper;
import com.mycode.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentByEntity (int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    public int findCountByEntity (int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

}
