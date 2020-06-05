package com.mycode.community.config;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Value("${aliyun.key.access}")
    private String accessKey;

    @Value("${aliyun.key.secret}")
    private String secretKey;

    @Value("${aliyun.bucket.header.endpoint}")
    private String headerBucketUrl;

    @Bean
    public OSSClient ossClient () {
        return new OSSClient(headerBucketUrl, accessKey, secretKey);
    }

}
