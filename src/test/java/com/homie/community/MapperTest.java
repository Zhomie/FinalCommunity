package com.homie.community;

import com.homie.community.dao.DiscussPostMapper;
import com.homie.community.dao.LoginTicketMapper;
import com.homie.community.dao.MessageMapper;
import com.homie.community.dao.UserMapper;
import com.homie.community.entity.DiscussPost;
import com.homie.community.entity.LoginTicket;
import com.homie.community.entity.Message;
import com.homie.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
    User user = userMapper.selectById(102);
        System.out.println(user);
    }
    @Test
    public void testSelectDiscussPost(){
    List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10,0);
        for (DiscussPost discussPost:list) {
            System.out.println(discussPost);
        }
    
    }
@Test
    public  void testLoginTicketMapper(){
          LoginTicket loginTicket = new LoginTicket();
          loginTicket.setUserId(1);
          loginTicket.setStatus(0);
          loginTicket.setTicket("abc");
          loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

          loginTicketMapper.insertLoginTicket(loginTicket);
}
    @Test
    public  void testSelectByTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket = loginTicketMapper.selectByTicket("abc");
        loginTicketMapper.updateStatus("abc",1);
    }

    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);

    }

}
