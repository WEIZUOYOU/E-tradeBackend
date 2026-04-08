// src/main/java/com/campus/trade/interceptor/LoginInterceptor.java
package com.campus.trade.interceptor;

import com.campus.trade.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行公开接口
        String uri = request.getRequestURI();
        if (uri.contains("/api/user/login") || uri.contains("/api/user/register") 
            || uri.contains("/api/product/list") || uri.contains("/api/product/detail")
            || uri.contains("/upload/") || uri.contains("/api/category")) {
            return true;
        }
        
        // 检查Session
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return true;
            }
        }
        
        // 未登录
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
        return false;
    }
}