package com.muke.gulimall.wms.vo;

import com.netflix.ribbon.Ribbon;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/7 10:15
 */
@Data
public class PurchaseDetailDoneVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 采购需求id
     */
    private Long itemId;

    /**
     * 采购需求状态
     */
    private Integer status;

    /**
     * 采购失败原因
     */
    private String reason;
}
