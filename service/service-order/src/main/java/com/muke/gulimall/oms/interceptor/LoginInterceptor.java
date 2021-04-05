package com.muke.gulimall.oms.interceptor;

import com.muke.common.vo.MemberRespVo;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 11:31
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取session中存放的用户信息
        MemberRespVo member = (MemberRespVo) request.getSession().getAttribute("member");
        if (member == null) {
            // 未登录，拒绝请求，重定向到登录页
            request.getSession().setAttribute("msg", "请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        } else {
            // 已登录，放行请求
            threadLocal.set(member);
            return true;
        }
    }
}