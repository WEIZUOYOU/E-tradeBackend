package com.campus.trade.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.campus.trade.common.Result;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. 优先从 Session 中获取用户 ID
        Object userId = request.getSession().getAttribute("userId");

        // 2. 兼容前端：如果 Session 里没有，尝试从请求头获取 Token (为以后改造成 JWT 留好后路)
        String authHeader = request.getHeader("Authorization");
        if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            // String token = authHeader.substring(7);
            // 这里可以预留解析 Token 的逻辑：userId = JwtUtil.parse(token); 
        }

        // 3. 如果最终依然没有获取到登录信息，返回 401
        if (userId == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            // 使用封装好的 Result，对齐前端要求的结构
            Result<Void> result = Result.error(401, "未登录或Token已过期");
            PrintWriter out = response.getWriter();
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();
            return false;
        }
        
        // 如果是从 Token 解析出来的 userId，为了方便后续 Controller 调用，塞入 request 中
        request.setAttribute("userId", userId);
        return true;
    }
}