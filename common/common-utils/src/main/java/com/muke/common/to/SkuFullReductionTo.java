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
public class SkuFullReductionTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer addOther;
}
