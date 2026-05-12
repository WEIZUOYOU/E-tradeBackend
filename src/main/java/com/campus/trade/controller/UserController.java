package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.request.LoginRequest;
import com.campus.trade.dto.request.RegisterRequest;
import com.campus.trade.dto.request.VerifyRequest;
import com.campus.trade.entity.User;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Long> register(@Validated @RequestBody RegisterRequest req) {
        Long userId = userService.register(req);
        return Result.success(userId);
    }

    @PostMapping("/login")
    public Result<User> login(@Validated @RequestBody LoginRequest req, HttpSession session) {
        User user = userService.login(req, session);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success(null);
    }

    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpSession session) {
        User user = userService.getCurrentUser(session);
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(user);
    }

    @PostMapping("/verify")
    public Result<Void> verify(@RequestBody @Validated VerifyRequest req, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        userService.verify(userId, req);
        return Result.success(null);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Validated @RequestBody com.campus.trade.dto.request.UpdateProfileRequest req,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        userService.updateProfile(userId, req, session);
        return Result.success(null);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String avatarUrl = userService.uploadAvatar(userId, file, session);
        return Result.success(avatarUrl);
    }

}