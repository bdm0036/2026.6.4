package com.bookstore.user.service;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.user.entity.User;
import com.bookstore.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public User getByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    public List<User> listAll() {
        List<User> users = userMapper.selectList(null);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    public void updateProfile(Long userId, User updateUser) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getEmail() != null) {
            user.setEmail(updateUser.getEmail());
        }
        if (updateUser.getPhone() != null) {
            user.setPhone(updateUser.getPhone());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
        }
        userMapper.updateById(user);
        log.info("用户 {} 更新个人信息", userId);
    }

    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("用户 {} 状态更新为: {}", userId, status);
    }
}
