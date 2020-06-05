package com.mycode.community;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayInputStream;
import java.io.File;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class OssTests {

    @Value("${aliyun.key.access}")
    private String accessKey;

    @Value("${aliyun.key.secret}")
    private String secretKey;

    @Value("${aliyun.bucket.header.name}")
    private String headerBucketName;

    @Value("${aliyun.bucket.header.endpoint}")
    private String headerBucketUrl;

    @Test
    public void testOSSUpload () {

        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = headerBucketUrl;
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = accessKey;
        String accessKeySecret = secretKey;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // <yourObjectName>表示上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        PutObjectRequest putObjectRequest = new PutObjectRequest(headerBucketName, "test02", new File("d:/work/data/wk-image/22c2f1974d904736ad6f9aee91633548.png"));

        // 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // metadata.setObjectAcl(CannedAccessControlList.Private);
        // putObjectRequest.setMetadata(metadata);

        // 上传字符串。
        ossClient.putObject(putObjectRequest);

        // 关闭OSSClient。
        ossClient.shutdown();

    }

}
