package com.muke.gulimall.pms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/17 18:48
 */
@Data
public class SeckillSkuRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private SeckillSkuRelationDto skuRelationDto;

    private Long startTime;

    private Long endTime;

    /**
     * 随机码
     */
    private String randomCode;
}
