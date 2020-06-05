package com.mycode.community.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OssService {

    private static final Logger logger = LoggerFactory.getLogger(OssService.class);

    @Value("${aliyun.key.access}")
    private String accessKey;

    @Value("${aliyun.key.secret}")
    private String secretKey;

    @Value("${aliyun.bucket.header.name}")
    private String headerBucketName;

    @Value("${aliyun.bucket.header.endpoint}")
    private String headerBucketUrl;

    @Value("${aliyun.policy.expire}")
    private long policyExpire;

    @Value("${aliyun.policy.maxSize}")
    private long policyMaxSize;

    @Autowired
    private OSSClient ossClient;

    /**
     *  生成签名（上传凭证）
     */
    public Map<String, String>  policy () {

        // host 格式为 http://bucketname.endpoint
        String host = "http://" + headerBucketName + "." + headerBucketUrl;
        // 用户上传文件时指定的前缀
        String dir = "user-header/";
        // 签名有效期
        long expireEndTime = System.currentTimeMillis() + policyExpire * 1000;
        Date expiration = new Date(expireEndTime);
        // 文件大小
        long maxSize = policyMaxSize * 1024 * 1024;

        Map<String, String> resultMap = new LinkedHashMap<>();

        try {
            PolicyConditions policyCond = new PolicyConditions();
            // 内容的长度范围-文件的大小
            policyCond.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, maxSize);
            policyCond.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            policyCond.addConditionItem(PolicyConditions.COND_SUCCESS_ACTION_STATUS, "200");

            String postPolicy = ossClient.generatePostPolicy(expiration, policyCond);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            // 设置响应信息,policy规定了请求的表单域的合法性
            String policy = BinaryUtil.toBase64String(binaryData);
            //根据Access Key Secret和policy计算的签名信息，OSS验证该签名信息从而验证该Post请求的合法性
            String signature = ossClient.calculatePostSignature(postPolicy);

            resultMap.put("accessid", ossClient.getCredentialsProvider().getCredentials().getAccessKeyId());
            resultMap.put("policy", policy);
            resultMap.put("signature", signature);
            resultMap.put("dir", dir);
            resultMap.put("host", host);
            resultMap.put("expire", String.valueOf(expireEndTime / 1000));

        } catch (UnsupportedEncodingException e) {
            logger.error("生成凭证失败：" + e);
        }

        return resultMap;
    }

}
