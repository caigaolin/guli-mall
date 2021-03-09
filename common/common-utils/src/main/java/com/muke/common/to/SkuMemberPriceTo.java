package com.muke.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 10:42
 */
@Data
public class SkuMemberPriceTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Long memberLevelId;

    private String memberLevelName;

    private BigDecimal memberPrice;

    private Integer addOther;
}
