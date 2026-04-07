package com.campus.trade.service;

import com.campus.trade.dto.LoginRequest;
import com.campus.trade.dto.RegisterRequest;
import com.campus.trade.dto.VerifyRequest;
import com.campus.trade.entity.User;
import com.campus.trade.exception.BusinessException;
import com.campus.trade.repository.SchoolUserRepository;
import com.campus.trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SchoolUserRepository schoolUserRepository;

    // 注册
    public Long register(RegisterRequest req) {
        // 检查手机号是否已存在
        User exist = userRepository.findByPhone(req.getPhone());
        if (exist != null) {
            throw new BusinessException("手机号已注册");
        }

        User user = new User();
        user.setStudentId(req.getStudentId());
        user.setUsername(req.getUsername());
        // 密码使用MD5加密（实际可改为BCrypt，此处简化）
        user.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setPhone(req.getPhone());
        user.setCreditScore(100);
        user.setStatus(0);
        return userRepository.insert(user);
    }

    // 登录
    public User login(LoginRequest req, HttpSession session) {
        User user = userRepository.findByPhone(req.getPhone());
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }
        String encryptedPwd = DigestUtils.md5DigestAsHex(req.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(encryptedPwd)) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException("账号已被冻结");
        }
        // 保存用户信息到session
        session.setAttribute("userId", user.getId());
        session.setAttribute("user", user);
        return user;
    }

    // 获取当前登录用户
    public User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId);
    }

    // 实名认证方法
    public void verify(Long userId, VerifyRequest req) {

        boolean exists = schoolUserRepository.exists(req.getStudentId(), req.getRealName());

        if (!exists) {
            throw new BusinessException("学号与姓名不匹配");
        }

        // 认证通过
        userRepository.updateVerify(userId, req.getStudentId(), req.getRealName(), 2);
    }

}
