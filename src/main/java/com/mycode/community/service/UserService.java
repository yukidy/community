package com.mycode.community.service;

import com.mycode.community.dao.UserMapper;
import com.mycode.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById (int id) {
        return userMapper.selectById(id);
    }
}
