package com.muke.gulimall.ums.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/2/26 14:51
 */
@FeignClient("service-coupon")
public interface CouponClient {

    @GetMapping("/sms/coupon/member")
    R getCouponByMember();
}
