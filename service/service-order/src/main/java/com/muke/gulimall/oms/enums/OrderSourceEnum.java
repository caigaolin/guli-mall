package com.muke.gulimall.oms.enums;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 22:11
 */
public enum OrderSourceEnum {
    /**
     * PC来源
     */
    PC(0),
    /**
     * app来源
     */
    APP(1)
    ;

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    OrderSourceEnum(Integer code) {
        this.code = code;
    }
}
