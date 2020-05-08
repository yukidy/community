package com.mycode.community.dao;

import com.mycode.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

//@Repository 访问数据库层时添加注解，但是mybatis开发有一个单独的注解
@Mapper   //两者都可以，在mybatis开发中倾向于@mapper注解
public interface UserMapper {
    //在Mapper中只需要写接口就行
    //声明一些增删改查的方法就可以，
    // 要想实现，需要给这个mapper提供一些配置文件，在配置文件(user-mapper.xml)中需要提供他所需要的每一个sql

    User selectById (int id); //通过id查询某用户

    User selectByName (String username); //通过用户名查询某用户

    User selectByEmail (String email); //通过mail查询某用户

    int insertUser (User user); //插入用户，返回用户列表行数

    int updateStatus (int id, int status); //修改用户状态，返回的是修改的条数

    int updateHeader (int id, String headerUrl); //修改用户头像

    int updatePassword (int id, String password); //修改用户密码

}
