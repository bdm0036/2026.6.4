package com.bookstore.user.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import com.bookstore.user.entity.User;
import com.bookstore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        User user = userService.getById(UserContext.getUserId());
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User updateUser) {
        userService.updateProfile(UserContext.getUserId(), updateUser);
        return Result.success("个人信息更新成功");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<Object> listAll() {
        return Result.success(userService.listAll());
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success("用户状态更新成功");
    }
}
