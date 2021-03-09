package com.muke.gulimall.wms.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/7 11:01
 */
@FeignClient("service-product")
public interface ProductFeign {

    @RequestMapping("/pms/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
