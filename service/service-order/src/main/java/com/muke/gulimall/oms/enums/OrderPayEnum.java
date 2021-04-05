package com.muke.gulimall.oms.enums;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 22:12
 */
public enum OrderPayEnum {

    /**
     * 支付宝
     */
    ALIPAY(1),
    /**
     * 微信
     */
    WECHAT(2),
    /**
     * 银联
     */
    UNIONPAY(3),
    /**
     * 货到付款
     */
    CASH(4)
    ;

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    OrderPayEnum(Integer code) {
        this.code = code;
    }
}
