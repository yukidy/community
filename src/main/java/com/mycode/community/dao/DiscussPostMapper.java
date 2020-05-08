package com.mycode.community.dao;

import com.mycode.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //分页查询帖子&查询我的帖子功能
    //首页分页查询，实际上不需要userId参数，这是给查询我的帖子预留的
    //不传值是为0，不拼到sql中，不为0时，拼到sql当中，做一个动态sql
    List<DiscussPost> selectDiscussPosts (int userId, int offset, int limit);  //offset:起始行行号; limit:每页行数

    //查询页码，同样包含两个功能首页分页和我的帖子分页
    // 一共可以有多少页-> 总贴数/分页行数
    // @Param注解用于给参数取别名，例如参数名过长，可以在@Param中修改简化参数名
    // 注意：如果只有一个参数，并且该方法所用到的sql是动态的（在<if>里使用），则必须加别名
    // 上面的方法也是做动态sql，但是它有三个参数，所以userId不需要起别名
    int selectDiscussPostRows (@Param("userId") int userId);

}
