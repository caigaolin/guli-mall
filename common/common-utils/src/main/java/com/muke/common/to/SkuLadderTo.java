package com.muke.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 10:41
 */
@Data
public class SkuLadderTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Integer fullCount;

    private BigDecimal discount;

    private BigDecimal price;

    private Integer addOther;
}
