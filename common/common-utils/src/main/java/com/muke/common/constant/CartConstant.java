package com.muke.common.constant;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/24 8:59
 */
public interface CartConstant {

    String TEM_USER_COOKIE_NAME = "user-key";

    Integer TEM_COOKIE_ALIVE_TIME = 60 * 60 * 24 * 30;

    String USER_CART_REDIS_PREFIX = "gulimall:cart:";
}
