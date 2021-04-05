package com.muke.gulimall.oms.enums;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 22:18
 */
public enum  BillTypeEnum {
    /**
     * 不开发票
     */
    NO_BILL(0),
    /**
     * 电子发票
     */
    ELECTRONIC_BILL(1),
    /**
     * 纸质发票
     */
    PAPER_BILL(2)
    ;

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    BillTypeEnum(Integer code) {
        this.code = code;
    }


}
