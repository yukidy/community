package com.mycode.community;

import com.mycode.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter () {

        String text = "这里可以赌博，可以嫖娼，可以吸&毒，可以开票，来不了，你妈的，shit！   fuck you!";
        text = sensitiveFilter.sensitivefilter(text);
        System.out.println(text);

    }

}
