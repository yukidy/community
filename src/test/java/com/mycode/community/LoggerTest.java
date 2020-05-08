package com.mycode.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {

    public static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger () {
        System.out.println(logger.getName());

        logger.debug("Debug Logger");
        logger.info("Info Logger");
        logger.warn("Warn Logger");
        logger.error("Error Logger");
    }

}
