package com.muke.gulimall.wms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/6 16:22
 */
@Data
public class PurchaseMergeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> items;

    private Long purchaseId;

}
