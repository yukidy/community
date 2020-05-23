package com.mycode.community.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

// spring 整合 es，所提供的这项技术，底层在访问es服务器时，会自动的将实体数据与es服务器的索引进行映射
// @Document -> 该实体：映射到哪个索引，映射的类型，映射时需要创建多少个分片，多少个副本，进行指定
// 将来在调用ElasticsearchRepository这个api时，如果没有相应的索引，分片，副本，会自动的根据配置去创建。
@Document(indexName = "discusspost", type = "_doc", shards = 6 , replicas = 3)
public class DiscussPost {

    // 主键
    @Id
    private int id;
    // 普通字段
    @Field(type = FieldType.Integer)
    private int userId;

    // title和content是重点，搜索的重点

    // analyzer:存储时的解析器， searchAnalyzer：搜索时的解析器

    // 例子：中华人民共和国国歌

    // -> 存储该条数据，建立索引，提炼出关键词，用关键词来关联这条数据
    // -> 为了搜索的范围扩大，应该尽可能的扩大关键词，拆分出更多的词条：中华人民共和国，国歌，中华人民，共和国，中华...
    // 所以说analyzer解析器（分词器）就应该选用一个范围非常大的解析器：中文分词器-ik_max_word

    // -> 而在搜索时，就没有必要拆分出那么多关键词进行搜索，中华人民共和国国歌这句话，搜索者想要的应该是和中国有关的内容，和国歌有关的内容，
    // -> 应该以一种聪明的方式，洞悉这句话的意图的方式去拆分，了解用户的搜索预期即可，拆分出尽可能少但满足需要的词汇
    // searchAnalyzer解析器（分词器）应该选用合理的：中文分词器-ik_smart

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private int type; //0-普通; 1-置顶;
    @Field(type = FieldType.Integer)
    private int status; // 0-正常; 1-精华; 2-拉黑;
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Integer)
    private int commentCount;
    @Field(type = FieldType.Double)
    private double score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DiscussPost{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", commentCount=" + commentCount +
                ", score=" + score +
                '}';
    }
}
