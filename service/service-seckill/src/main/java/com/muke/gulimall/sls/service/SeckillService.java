package com.muke.gulimall.sls.service;

import com.muke.gulimall.sls.dto.SeckillSkuRedisDTO;
import com.muke.gulimall.sls.vo.SeckillProductVo;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/13 22:40
 */
public interface SeckillService {
    void upProducts3Days();

    List<SeckillSkuRedisDTO> getCurrentSeckillInfo();

    SeckillSkuRedisDTO getSeckillInfoBySkuId(Long skuId);

    String killProduct(String key, String code, Integer num);
}
