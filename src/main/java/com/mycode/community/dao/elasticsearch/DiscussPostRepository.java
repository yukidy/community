package com.mycode.community.dao.elasticsearch;

import com.mycode.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

    // 只需要继承一个接口ElasticsearchRepository<T, ID>，
    // 继承时声明好泛型，确认该接口处理的实体类类型，声明主键类型

}
