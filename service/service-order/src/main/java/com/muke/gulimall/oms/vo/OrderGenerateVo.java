package com.muke.gulimall.oms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 16:47
 */
@Data
public class OrderGenerateVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 地址id
     */
    private Long addrId;

    /**
     * 防重令牌
     */
    private String token;

    /**
     * 应付价格
     */
    private BigDecimal payPrice;
}
