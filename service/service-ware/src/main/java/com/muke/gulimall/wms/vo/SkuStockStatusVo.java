package com.muke.gulimall.wms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 15:52
 */
@Data
public class SkuStockStatusVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Long stock;
}
