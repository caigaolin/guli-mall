package com.muke.gulimall.wms.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/5 20:09
 */
@FeignClient("service-order")
public interface OrderFeign {

    @GetMapping("/oms/order/order-sn/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
