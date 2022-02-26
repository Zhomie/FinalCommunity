package com.homie.community.service;

import com.homie.community.dao.LoginTicketMapper;
import com.homie.community.dao.UserMapper;
import com.homie.community.entity.LoginTicket;
import com.homie.community.entity.User;
import com.homie.community.util.CommunityConstant;
import com.homie.community.util.CommunityUtil;
import com.homie.community.util.MailClient;
import com.homie.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
   /* @Autowired
    private LoginTicketMapper loginTicketMapper;*/
    @Autowired
    private UserMapper userMapper;
    //注册需要发邮件，讲邮件发送注入进来
    @Autowired
    private MailClient mailClient;
    //还需要一个模板引擎
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private  String domain;
    @Value("${server.servlet.context-path}")
    private  String contextPath;

    public User findUserById(int userId){
       // return userMapper.selectById(userId);
        User user = getCache(userId);
        if(user==null){
            user = initCache(userId);
        }
        return  user;
    }

    public int updateHeaderUrl(int userId,String headerUrl){
       // return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clear(userId);
        return  rows;
    }

    public int updatePassword(int userId,String newPassword){
        return userMapper.updatePassword(userId,newPassword);
    }

    //返回的主要是错误信息，账号不存在，不能为空等。
public Map<String ,Object> register(User user){
    Map<String,Object> map = new HashMap<>();

    //对空值进行判断。
    if(user==null){
        throw new IllegalArgumentException("参数不能为空！");
    }
    if(StringUtils.isBlank(user.getUsername())){
        map.put("usernameMsg","账号不能为空");
        return map;
    }
    if(StringUtils.isBlank(user.getPassword())){
        map.put("passwordMsg","密码不能为空");
        return map;
    }
    if(StringUtils.isBlank(user.getEmail())){
        map.put("emailMsg","邮箱不能为空");
        return map;
    }
    //验证号码
    User u = userMapper.selectByName(user.getUsername());
    if(u!=null){
        map.put("usernameMsg","用户名已存在");
    }
    //邮箱验证
    u = userMapper.selectByEmail(user.getEmail());
    if(u!=null){
        map.put("emailMsg","邮箱已存在");
    }
    //注册用户
    user.setSalt(CommunityUtil.generateUUID().substring(0,5));
    user.setPassword((CommunityUtil.md5(user.getPassword()+user.getSalt())));
    user.setType(0);
    user.setStatus(0);
    user.setActivationCode(CommunityUtil.generateUUID());
    user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
    user.setCreateTime(new Date());
    userMapper.insertUser(user);

    //激活邮件
    Context context= new Context();
    context.setVariable("email",user.getEmail());
    //http://localhost:8080/community/activation/101/code
    String url = domain+contextPath+"/activation/"+user.getId()+user.getActivationCode();
    context.setVariable("url",url);
    //利用模板引擎生成邮件内容。
    String content = templateEngine.process("/mail/activation",context);
    mailClient.sendMail(user.getEmail(),"激活账号",content);
    return map;
}

public int activation(int userId,String code){
    User user = userMapper.selectById(userId);
    if(userId==1){
        return ACTIVATION_REPEAT;
    }
    else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            //上面的userMapper代码对用户进行了修改，就需要清除缓存
            clear(userId);
        return ACTIVATION_SUCCESS;
    }else{
            return  ACTIVATION_FAILURE;
    }
}

public Map<String ,Object> login(String username,String password,int expiredSecond){
    Map<String ,Object> map = new HashMap<>();
    //空值处理
    if(StringUtils.isBlank(username)){
        map.put("usernameMsg","账号不能为空");
        return map;
    }
    if(StringUtils.isBlank(password)){
        map.put("passwordMsg","密码不能为空");
        return map;
    }
    //账号验证
    User user = userMapper.selectByName(username);
    if(user==null){
        map.put("usernameMsg","该账号不存在");
        return map;
    }
    //验证状态
    if(user.getStatus()==0){
        map.put("usernameMsg","该账号未激活");
        return map;
    }
    //验证密码
    password = CommunityUtil.md5(password + user.getSalt());
    if(!user.getPassword().equals(password)){
        map.put("passwordMsg","密码错误");
        return map;
    }
    //生成登陆凭证
    LoginTicket loginTicket= new LoginTicket();
    loginTicket.setUserId(user.getId());
    loginTicket.setTicket(CommunityUtil.generateUUID());
    loginTicket.setStatus(0);
    loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSecond*1000));
    //loginTicketMapper.insertLoginTicket(loginTicket);
    String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
    redisTemplate.opsForValue().set(redisKey,loginTicket);

    map.put("ticket",loginTicket.getTicket());
        return map;
}
//退出需要把凭证传给我，告诉我是谁
public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);
    String redisKey = RedisKeyUtil.getTicketKey(ticket);
    LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    loginTicket.setStatus(1);
    redisTemplate.opsForValue().set(redisKey,loginTicket);
}

public LoginTicket findLoginTicket(String ticket){
       // LoginTicket loginTicket=loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
    return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
}
    // 重置密码
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }

        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);

        map.put("user", user);
        return map;
    }

    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        //isBlank()用来判空，比isEmpty()更实用
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        //验证输入的旧密码与数据库中的旧密码是否相同。
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        return map;
    }
    public User findUerByName(String username){
        return userMapper.selectByName(username);
    }
    //1.优先从缓存中取值
    private  User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserkey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到时初始化缓存数据
    private  User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserkey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return  user;
    }
    //3.当数据变更时，清除缓存（更新缓存可能导致并发问题，两个人同时更新）
    private void clear(int userId){
        String redisKey = RedisKeyUtil.getUserkey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1 :
                            return AUTHORITY_ADMIN;
                    case  2 :
                            return AUTHORITY_MODERATOR;
                    default:
                            return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
