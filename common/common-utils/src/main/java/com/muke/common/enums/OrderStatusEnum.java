package com.muke.common.enums;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 21:58
 */
public enum OrderStatusEnum {
    /**
     * 待付款
     */
    WAIT_PAY(0),
    /**
     * 待发货
     */
    WAIT_SHIP(1),
    /**
     * 已发货
     */
    ALREADY_SHIP(2),
    /**
     * 已完成
     */
    FINISHED_ORDER(3),
    /**
     * 已关闭
     */
    CLOSED_ORDER(4),
    /**
     * 无效订单
     */
    INVALID_ORDER(5)
    ;

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    OrderStatusEnum(Integer code) {
        this.code = code;
    }
}
