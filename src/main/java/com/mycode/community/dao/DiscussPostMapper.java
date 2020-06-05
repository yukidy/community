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

    //  List<DiscussPost> selectDiscussPosts (int userId, int offset, int limit);  //offset:起始行行号; limit:每页行数

    // 重构-加入首页热帖
    // 默认orderMode:0 按照类型和时间排序  orderMode:1 按照帖子热度排序
    List<DiscussPost> selectDiscussPosts (int userId, int offset, int limit, int orderMode);

    //查询页码，同样包含两个功能首页分页和我的帖子分页
    // 一共可以有多少页-> 总贴数/分页行数
    // @Param注解用于给参数取别名，例如参数名过长，可以在@Param中修改简化参数名
    // 注意：如果只有一个参数，并且该方法所用到的sql是动态的（在<if>里使用），则必须加别名
    // 上面的方法也是做动态sql，但是它有三个参数，所以userId不需要起别名
    int selectDiscussPostRows (@Param("userId") int userId);

    // 增加帖子
    int insertDiscussPost (DiscussPost discussPost);

    // 查找帖子详情
    DiscussPost selectDiscussPostById (int id);

    // 更新评论数commentCount
    int updateCommentCount (int id, int commentCount);

    // 更改帖子类型 （0-普通、1-置顶）
    int updateDiscussPostType (int postId, int type);

    // 更改帖子状态 （0-正常、1-加精、2-拉黑）
    int updateDiscussPostStatus (int postId, int status);

    int updateScore (int postId, double score);

    // 批量插入-测试 for循环拼接
    int insertBatchDiscussPosts (List<DiscussPost> list);

}
