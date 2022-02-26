package com.homie.community.util;

import com.homie.community.entity.User;
import org.springframework.stereotype.Component;
/**
 *  持有用户信息
 *  代替session
 *
 * */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();
    public  void setUser(User user){
        users.set(user);
    }

    public User getUsers() {
        return users.get();
    }
    public void Clear(){
        users.remove();
    }
}
