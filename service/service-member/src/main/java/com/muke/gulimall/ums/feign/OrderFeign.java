package com.muke.gulimall.ums.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/12 12:54
 */
@FeignClient("service-order")
public interface OrderFeign {

    @PostMapping("/oms/order/list")
    R list(@RequestBody Map<String, Object> params);
}
