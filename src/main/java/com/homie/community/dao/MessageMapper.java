package com.homie.community.dao;

import com.homie.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量.
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表.
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量.
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);
    //新增一个消息
    int insertMessage(Message message);

    //未读消息改为已读消息
    int updateStatus(List<Integer> ids,int status);

    //查询某个主题下最新通知
    Message selectLatestNotice(int userId,String topic);

    //查询某个主题下的通知总数
    int selectNoticeCount(int userId,String topic);

    //查询某个主题下的未读通知数量
    int selectNoticeUnreadCount(int userId,String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}