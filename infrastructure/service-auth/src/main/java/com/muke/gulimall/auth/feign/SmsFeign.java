package com.muke.gulimall.auth.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/17 14:16
 */
@FeignClient("third-party")
public interface SmsFeign {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
