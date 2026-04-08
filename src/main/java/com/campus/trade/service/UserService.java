package com.campus.trade.service;

import com.campus.trade.dto.LoginRequest;
import com.campus.trade.dto.RegisterRequest;
import com.campus.trade.dto.VerifyRequest;
import com.campus.trade.entity.User;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.SchoolUserRepository;
import com.campus.trade.repository.UserRepository;
import com.campus.trade.utils.PasswordUtil; // 引入新工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SchoolUserRepository schoolUserRepository;

    // 注册
    public Integer register(RegisterRequest req) {
        // 1. 检查手机号和学号是否已存在
        if (userRepository.findByPhone(req.getPhone()) != null) {
            throw new BusinessException("手机号已注册");
        }
        if (userRepository.findByStudentId(req.getStudentId()) != null) {
            throw new BusinessException("学号已注册");
        }

        User user = new User();
        user.setStudentId(req.getStudentId());
        user.setUsername(req.getUsername());
        
        // 2. 使用更安全的 BCrypt 加密
        user.setPassword(PasswordUtil.encode(req.getPassword()));
        
        user.setPhone(req.getPhone());
        user.setStatus(1); // 1-正常 (匹配之前的常量定义)
        user.setIsAuth(0); // 初始未认证
        
        return userRepository.insert(user);
    }

    // 登录
    public User login(LoginRequest req, HttpSession session) {
        // 校园项目通常支持 学号 或 手机号 登录，这里以手机号为例
        User user = userRepository.findByPhone(req.getPhone());
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        
        // 3. 使用 PasswordUtil 校验密文
        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被冻结");
        }

        // 保存用户信息到session
        session.setAttribute("userId", user.getId());
        session.setAttribute("user", user);
        return user;
    }

    // 获取当前登录用户
    public User getCurrentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId);
    }

    // 实名认证方法
    public void verify(Integer userId, VerifyRequest req) {
        // 4. 调用学校数据库比对
        boolean exists = schoolUserRepository.exists(req.getStudentId(), req.getRealName());

        if (!exists) {
            throw new BusinessException("学号与姓名在学校数据库中不匹配");
        }

        // 认证通过，更新用户表的认证状态和真实姓名
        userRepository.updateVerify(userId, req.getStudentId(), req.getRealName(), 1); // 1 代表已认证
    }
}