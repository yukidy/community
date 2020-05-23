package com.mycode.community.service;

import com.mycode.community.dao.elasticsearch.DiscussPostRepository;
import com.mycode.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    /**
     *  向ES服务器提交新产生的帖子
     */
    public void saveDiscussPost (DiscussPost discussPost) {
        discussRepository.save(discussPost);
    }

    /**
     *  删除ES服务器中的帖子
     *      通过帖子id删除
     */
    public void deleteDiscussPost (int discussPostId) {
        discussRepository.deleteById(discussPostId);
    }

    /**
     *  搜索方法
     */
    public Page<DiscussPost> searchDiscussPost (String keyWord, int current, int limit) {

        // 构造查询条件
        SearchQuery searchQuery = discussPostSearchQuery(keyWord, current, limit);

        // 自定义查询结果封装，实现高亮显示，实现SearchResultMapper接口
        return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {

            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                // queryForPage得到的结果会自动的交给SearchResultMapper处理，searchResponse得到这些参数值
                // 利用这些结果再封装返回即可

                // 得到查询结果命中的数据
                SearchHits hits = searchResponse.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }
                // 封装的高亮数据
                List<DiscussPost> list = hightLightProcess(hits);

                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(),
                        searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                return null;
            }

        });

    }

    // 构造查询条件
    private SearchQuery discussPostSearchQuery (String keyWord, int current, int limit) {
        // 构造查询条件
        return new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyWord, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
    }

    // 自定义查询结果封装，实现高亮显示，实现SearchResultMapper接口
    private List<DiscussPost> hightLightProcess (SearchHits hits) {

        // 将数据封装到集合
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            DiscussPost post = new DiscussPost();

            String id = hit.getSourceAsMap().get("id").toString();
            post.setId(Integer.valueOf(id));

            String userId = hit.getSourceAsMap().get("userId").toString();
            post.setUserId(Integer.valueOf(userId));

            String title = hit.getSourceAsMap().get("title").toString();
            post.setTitle(title);

            String content = hit.getSourceAsMap().get("content").toString();
            post.setContent(content);

            String status = hit.getSourceAsMap().get("status").toString();
            post.setStatus(Integer.valueOf(status));

            String createTime = hit.getSourceAsMap().get("createTime").toString();
            post.setCreateTime(new Date(Long.valueOf(createTime)));

            String commentCount = hit.getSourceAsMap().get("commentCount").toString();
            post.setCommentCount(Integer.valueOf(commentCount));

            String score = hit.getSourceAsMap().get("score").toString();
            post.setScore(Double.valueOf(score));

            // 处理高亮显示
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                // getFragments返回的是数值，因为高亮显示可能不止一个,
                // 如果匹配多个，就只选择一个高亮显示就行，选择第一个
                post.setTitle(titleField.getFragments()[0].toString());
            }

            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                // getFragments返回的是数值，因为高亮显示可能不止一个,
                // 如果匹配多个，就只选择一个高亮显示就行，选择第一个
                post.setContent(contentField.getFragments()[0].toString());
            }

            list.add(post);
        }

        return list;
    }




}
