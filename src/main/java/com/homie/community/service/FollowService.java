package com.homie.community.service;

import com.homie.community.entity.User;
import com.homie.community.util.CommunityConstant;
import com.homie.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FollowService implements CommunityConstant {
        @Autowired
        private RedisTemplate redisTemplate;
        @Autowired
        private UserService userService;

        public void follow(int userId,int entityType,int entityId){
                redisTemplate.execute(new SessionCallback() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                        operations.multi();

                        operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                        operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                        return operations.exec();
                    }
                });
        }

    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();

                operations.opsForZSet().remove(followerKey,userId);
                operations.opsForZSet().remove(followeeKey,entityId);
                return operations.exec();
            }
        });
    }
    //查询某个用户关注实体的数量
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //某个实体的粉丝数量
    public long findFollowerCount(int entityType,int entityId){
            String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
            return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //查询当前用户是否关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }
    //查询当前用户的关注
    public List<Map<String ,Object>> findFollowees(int userId, /*支持分页*/int offset , int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey,offset,offset+limit-1);
        if(targetIds==null){
            return null;
        }
        List<Map<String ,Object>> list = new ArrayList<>();
        for (Integer targetId:targetIds){
                Map<String ,Object> map = new HashMap<>();
               User user = userService.findUserById(targetId);
                map.put("user",user);
                Double score = redisTemplate.opsForZSet().score(followeeKey,targetId);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
        }
            return list;
        }
    //查询某个用户的粉丝
    public List<Map<String ,Object>> findFollowers(int userId, /*支持分页*/int offset , int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(userId,ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit-1);
        if(targetIds==null){
            return null;
        }
        List<Map<String ,Object>> list = new ArrayList<>();
        for(Integer targetId :targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double sore = redisTemplate.opsForZSet().score(followerKey,targetId);
            map.put("followTime",new Date(sore.longValue()));
            list.add(map);
        }
        return list;
    }

}
