package com.muke.gulimall.oms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 15:32
 */
@Data
public class CartItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Boolean isChecked = true;

    private String title;

    private String image;

    private List<String> skuAttrs;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    private BigDecimal amount = new BigDecimal("0");
}
