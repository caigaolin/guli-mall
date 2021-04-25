package com.muke.gulimall.sls.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/15 19:59
 */
@Component
@FeignClient(value = "service-coupon")
public interface CouponFeign {

    @GetMapping("/coupon/seckillsession/recent-3days/session")
    R getRecent3DaysSession();

}
