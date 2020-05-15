package com.mycode.community;

import com.mycode.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TranscationTests {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void testSave1 () {
        Object obj1 = alphaService.save1();
        System.out.println(obj1);
    }

    @Test
    public void testSave2 () {
        Object obj1 = alphaService.save2();
        System.out.println(obj1);
    }

}
