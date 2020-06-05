package com.mycode.community.actuator;

import com.mycode.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  自定义Actuator端点-获取数据库连接的状态-管理员权限
 *      /actuator/database
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    // 注入sql的数据源
    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection () {

        try (
                // 判断数据连接状态
                Connection conn = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "数据库连接成功！");
        } catch (SQLException e) {
            logger.error("获取数据库连接失败: " + e.getMessage());
            return CommunityUtil.getJSONString(1, "数据库连接失败！");
        }

    }

}
