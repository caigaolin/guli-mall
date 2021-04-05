package com.muke.gulimall.oms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 16:55
 */
@Data
public class OrderRespVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private String orderSn;

    private BigDecimal payPrice;

}
