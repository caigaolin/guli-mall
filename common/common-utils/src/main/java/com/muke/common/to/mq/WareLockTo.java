package com.muke.common.to.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/5 16:57
 */
@Data
public class WareLockTo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 库存工作单详情
     */
    private WareDetailTaskTo wareDetailTaskTo;
}
