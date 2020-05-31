package com.mycode.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    // 把配置文件创建的路径注入到该配置类中
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     *  在服务启动时，初始化，创建wkhtmltoimage存储图片的路径
     */
    @PostConstruct
    public void init () {
        // 创建WK图片目录
        File file = new File(wkImageStorage);   // 这个file不是普通文件，而是一个路径
        if (!file.exists()) {
            file.mkdir();
            logger.info("创建WK图片目录：" + wkImageStorage);
        }
    }

}
