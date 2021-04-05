package com.muke.gulimall.cart.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/24 17:24
 */
@FeignClient("service-product")
public interface ProductFeign {

    @RequestMapping("/pms/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/pms/skusaleattrvalue/{skuId}")
    List<String> getAttrNameAndValue(@PathVariable("skuId") Long skuId);

}
