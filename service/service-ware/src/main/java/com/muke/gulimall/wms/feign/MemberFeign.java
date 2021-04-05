package com.muke.gulimall.wms.feign;

import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 13:00
 */
@FeignClient("service-member")
public interface MemberFeign {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}
