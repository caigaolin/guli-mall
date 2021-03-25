package com.muke.gulimall.cart.interceptor;

import com.muke.common.constant.AuthConstant;
import com.muke.common.constant.CartConstant;
import com.muke.common.vo.MemberRespVo;
import com.muke.gulimall.cart.vo.MemberInfoVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Random;
import java.util.UUID;

/**
 * 购物车拦截器
 * @author 木可
 * @version 1.0
 * @date 2021/3/23 21:03
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberInfoVo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MemberRespVo member = (MemberRespVo) request.getSession().getAttribute(AuthConstant.USER_SESSION_NAME);
        MemberInfoVo infoVo = new MemberInfoVo();
        if (member != null) {
            // 用户已登录,设置用户id
            infoVo.setUserId(member.getId());
        }
        // 无论用户是否登录，等需要生成userKey
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            // 判断cookie中是否存在user-key
            if (cookie.getName().equals(CartConstant.TEM_USER_COOKIE_NAME)) {
                // 设置用户key
                infoVo.setUserKey(cookie.getValue());
                infoVo.setIsTemUser(false);
            }
        }

        //如果第一次使用购物车，需给一个临时用户
        if (StringUtils.isEmpty(infoVo.getUserKey())) {
            String userKey = UUID.randomUUID().toString().replace("-", "");
            infoVo.setUserKey(userKey);
            infoVo.setIsTemUser(true);
        }
        // 将用户信息放入threadLocal中
        threadLocal.set(infoVo);
        return true;
    }

    /**
     * 目标方法之后
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        MemberInfoVo memberInfoVo = threadLocal.get();
        // 判断是否存在临时用户
        if (memberInfoVo.getIsTemUser()) {
            // 不存在将临时用户的userKey写到浏览器
            Cookie cookie = new Cookie(CartConstant.TEM_USER_COOKIE_NAME, memberInfoVo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEM_COOKIE_ALIVE_TIME);
            // 将cookie写到浏览器
            response.addCookie(cookie);
        }
    }
}
