package com.muke.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/6 10:25
 */
@Data
public class SkuLadderTo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long skuId;
    /**
     * 满减数量
     */
    private int fullCount;
    /**
     * 满减折扣
     */
    private BigDecimal discount;
    /**
     * 折后价
     */
    private BigDecimal price;
    /**
     * 是否可数量满减叠加 [1:可叠加；0:不可叠加]
     */
    private int addOther;
}
