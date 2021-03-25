package com.muke.common.to.es;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 14:27
 */
@Data
public class SpuUpEsTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spuId;

    private Long skuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long catelogId;

    private String catelogName;

    private Long brandId;

    private String brandName;

    private String brandImg;

    private List<SpuAttrTo> attrs;

}
