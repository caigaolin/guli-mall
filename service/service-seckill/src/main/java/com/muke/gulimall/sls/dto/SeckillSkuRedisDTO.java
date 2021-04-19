package com.muke.gulimall.sls.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/15 20:59
 */
@Data
public class SeckillSkuRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private SeckillSkuRelationDto skuRelationDto;

    private SkuInfoDto skuInfoDto;

    private Long startTime;

    private Long endTime;

    /**
     * 随机码
     */
    private String randomCode;
}
