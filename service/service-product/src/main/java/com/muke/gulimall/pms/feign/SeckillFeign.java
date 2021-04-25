package com.muke.gulimall.pms.feign;

import com.muke.common.utils.R;
import com.muke.gulimall.pms.feign.impl.SeckillFeignImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/17 18:45
 */
@FeignClient(value = "service-seckill", fallback = SeckillFeignImpl.class)
public interface SeckillFeign {

    @GetMapping("/sls/seckill-info/{skuId}")
    R getSeckillInfoBySkuId(@PathVariable("skuId") Long skuId);
}
