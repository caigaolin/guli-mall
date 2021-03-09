package com.muke.common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 15:11
 */
@Data
public class SkuStockStatusTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Boolean stockStatus;
}
