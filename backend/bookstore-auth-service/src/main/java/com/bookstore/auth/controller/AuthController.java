package com.bookstore.auth.controller;

import com.bookstore.auth.service.AuthService;
import com.bookstore.common.dto.LoginDTO;
import com.bookstore.common.dto.RegisterDTO;
import com.bookstore.common.entity.Result;
import com.bookstore.common.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = authService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Map<String, Object> result = authService.register(registerDTO);
        return Result.success("注册成功", result);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(UserContext.getUserId());
        return Result.success("已退出登录");
    }
}
