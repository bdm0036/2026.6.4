package com.bookstore.auth.service;

import com.bookstore.auth.entity.User;
import com.bookstore.auth.mapper.UserMapper;
import com.bookstore.common.dto.LoginDTO;
import com.bookstore.common.dto.RegisterDTO;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthService(UserMapper userMapper,
                       @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    private boolean hasRedis() { return redisTemplate != null; }

    public Map<String, Object> login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getPassword, password));

        if (user == null) throw new BusinessException(401, "用户名或密码错误");
        if (user.getStatus() == 0) throw new BusinessException(403, "账号已被禁用");

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        if (hasRedis()) redisTemplate.opsForValue().set("token:" + user.getId(), token, 24, TimeUnit.HOURS);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());

        log.info("用户 {} 登录成功", username);
        return result;
    }

    public Map<String, Object> register(RegisterDTO registerDTO) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) throw new BusinessException("用户名已存在");

        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes(StandardCharsets.UTF_8)))
                .email(registerDTO.getEmail()).phone(registerDTO.getPhone())
                .nickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername())
                .status(1).role("USER").build();

        userMapper.insert(user);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());

        log.info("新用户注册: {}", user.getUsername());
        return result;
    }

    public void logout(Long userId) {
        if (hasRedis()) redisTemplate.delete("token:" + userId);
        log.info("用户 {} 已登出", userId);
    }
}
