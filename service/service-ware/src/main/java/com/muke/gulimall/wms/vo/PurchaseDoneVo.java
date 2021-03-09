package com.muke.gulimall.wms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/7 10:14
 */
@Data
public class PurchaseDoneVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采购单id
     */
    private Long id;

    /**
     * 完成/失败的需求详情
     */
    private List<PurchaseDetailDoneVo> items;

}
