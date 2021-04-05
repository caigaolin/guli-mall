package com.muke.gulimall.oms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/4 14:53
 */
@Data
public class WareLockDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Integer number;

}
