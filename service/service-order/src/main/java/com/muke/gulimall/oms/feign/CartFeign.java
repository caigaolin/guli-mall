package com.muke.gulimall.oms.feign;

import com.muke.gulimall.oms.vo.CartItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 17:11
 */
@FeignClient("service-cart")
public interface CartFeign {

    @GetMapping("/get/cartItems")
    List<CartItemVo> getCartItemsByKey();
}
