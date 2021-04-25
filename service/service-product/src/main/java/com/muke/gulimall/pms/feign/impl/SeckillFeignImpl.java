package com.muke.gulimall.pms.feign.impl;

import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.utils.R;
import com.muke.gulimall.pms.feign.SeckillFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/19 21:53
 */
@Slf4j
@Component
public class SeckillFeignImpl implements SeckillFeign {

    @Override
    public R getSeckillInfoBySkuId(Long skuId) {
        log.error("getSeckillInfoBySkuId熔断了。。。。");
        return R.error(CustomizeExceptionEnum.FEIGN_EX);
    }
}
