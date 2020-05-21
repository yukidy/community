package com.mycode.community.dao;

import com.mycode.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.test.context.jdbc.Sql;

@Mapper
// 若某类或某方法加上 @Deprecated 该注解之后，表示此方法或类不再建议使用，
// 调用时也会出现删除线，但并不代表不能用，只是说，不推荐使用，因为还有更好的方法可以调用
@Deprecated
public interface LoginTickerMapper {

    /**
     *  登录凭证使用redis
     */

    /**
     * 方法一：此前，在数据访问层声明了方法后，都是在Mapper中新建配置文件.xml后写sql
     *  方法二：而除了该方法之外，还有一种方式是：
     *      在接口当中写注解，通过注解去声明对应的方法去写sql
     *
     *  该接口使用第二种方式
     *  目的：学习两种不同的方式去写数据访问层
     *
     *  此用法的一些缺点：sql复杂时，没有比xml文件更舒服，阅读起来麻烦
     *  优点：书写方便简单
     */

    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket (LoginTicket loginTicket);   //将ticket信息存入数据库

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket ",
            "where ticket = #{ticket} "
    })
    LoginTicket selectByTicket (String ticket);   //通过ticket查找ticket信息

    @Update({
            "<script> ",
            "update login_ticket ",
            "set status = #{status} ",
            "where ticket = #{ticket} ",
            //通过注解的方式同样可以使用条件判断等语句，下面为一个小事例，不对更新操作产生影响
            "<if test = \"ticket != null\"> ",
                "and 1 = 1 ",
            "</if> ",
            "</script>"
    })
    int updateStatus (String ticket, int status);    //修改ticket凭证状态
}
