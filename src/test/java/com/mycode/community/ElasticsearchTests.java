package com.mycode.community;

import com.mycode.community.dao.DiscussPostMapper;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    // ES的数据来源于mysql，从mysql中取到数据再转存到ES中

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    /**
     *  插入数据
     */
    @Test
    public void testInsert () {

        discussRepository.save(discussMapper.selectDiscussPostById(230));
        discussRepository.save(discussMapper.selectDiscussPostById(231));
        discussRepository.save(discussMapper.selectDiscussPostById(232));

    }

    /**
     *  插入多条数据
     */
    @Test
    public void testInsertList () {

        discussRepository.saveAll(discussMapper.selectDiscussPosts(101, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134, 0, 100, 0));

    }

    /**
     *  修改数据
     */
    @Test
    public void testUpdate () {

        DiscussPost discussPost = discussMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人，已修改！");
        discussRepository.save(discussPost);

    }

    /**
     *  删除数据
     */
    @Test
    public void testDelete () {

        // 通过主键id
        // discussRepository.deleteById(231);

        // 删除所有，比较危险
        discussRepository.deleteAll();

    }


    /**
     * 利用 Repository接口 进行搜索
     */
    @Test
    public void testSearchByRepository () {

        // 搜索时：首先要构造搜索条件，
        // 搜索之后的数据是否需要排序、分页，
        // 结果是否需要高亮显示（对搜索出的内容里有的关键词加粗标记），ES会对内容中的关键词左右添加标签，标签可自定义
        // 如：搜索"互联网" -> <em>互联网</em>是什么呢？ -> 有一个标签区分

        // 所以说，构造搜索条件需要一系列的内容

        // spring整合es所提供的组件：构造搜索条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                // withQuery：构建搜索条件，同时需要另一个对象QueryBuilders构造，
                // multiMatchQuery：多字段搜索 -> ("搜索关键词", "搜索字段", "搜索字段", ...)
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 排序条件：withSort，使用SortBuilders对象构建，fieldSort：需要排序的字段，order：排序顺序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页：withPageable，使用PageRequest对象，of(0, 10)表示当前查询的是第几页，和多少条数据
                .withPageable(PageRequest.of(0, 10))
                // 指定高亮显示：withHighlightFields，返回结果的哪些字段需要高亮显示，使用HighlightBuilder构建
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build(); // 构建

        // 该Page对象存储了多个实体，当前搜索的一页的数据都存储在page中（类似一个List）
        Page<DiscussPost> page = discussRepository.search(searchQuery);

        System.out.println(page.get());
        System.out.println(page.getTotalElements()); // 查询结果总数
        System.out.println(page.getTotalPages()); // 总共分几页
        System.out.println(page.getNumber()); // 当前处于第几页
        System.out.println(page.getSize()); // 每页显示多少数据

        // page继承了Iterator接口
        for (DiscussPost post : page) {

            System.out.println(post);

        }

        // 查询结果并没有在关键词上戴上<em>标签
        // Repository底层的默认实现类，从ES查询到结果时，是有返回了带标签的内容，
        // 但是它没有将内容合并到系统的查询结果中，page返回的内容是原始的结果，Repository同样也有带有标签的内容，有两份数据
        // 我们需要将原始的结果和标签内容进行一个替换

        // Repository在search()时，底层调用了elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)方法
        // searchQuery:搜索条件，
        // class：需要查询的实体类型，
        // SearchResultMapper：queryForPage查到的数据，由它来处理，但是没有进行处理

    }

    /**
     *  所以说，直接使用Repository方案查询结果，底层是有缺陷的
     *
     *  解决方案
     *      直接使用elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)方法
     *      对SearchResultMapper进行处理，实现该接口
     */


    @Test
    public void testSearchByTemplate () {

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        // 自定义查询结果封装，实现高亮显示，实现SearchResultMapper接口
        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                // queryForPage得到的结果会自动的交给SearchResultMapper处理，searchResponse得到这些参数值
                // 利用这些结果再封装返回即可

                // 得到查询结果命中的数据
                SearchHits hits = searchResponse.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                // 将数据封装到集合
                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setId(Integer.valueOf(userId));

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

                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                return null;
            }
        });

        System.out.println(page.getTotalElements()); // 查询结果总数
        System.out.println(page.getTotalPages()); // 总共分几页
        System.out.println(page.getNumber()); // 当前处于第几页
        System.out.println(page.getSize()); // 每页显示多少数据

        // page继承了Iterator接口
        for (DiscussPost post : page) {

            System.out.println(post);

        }

    }



}
