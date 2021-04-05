package com.muke.gulimall.wms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/4 14:57
 */
@Data
public class WareLockDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Integer number;
}
