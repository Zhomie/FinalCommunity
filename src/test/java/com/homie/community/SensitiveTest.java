package com.homie.community;

import com.homie.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;
    private String string;

    @Test
    public void SensTest(){
        String text = "这里可以赌博，可以嫖娼，可以吸毒，可以开票！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

         text = "这里可以☆赌☆博☆，可以☆嫖☆☆☆☆☆娼☆，可以☆吸☆毒☆，可以☆开☆票☆！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
