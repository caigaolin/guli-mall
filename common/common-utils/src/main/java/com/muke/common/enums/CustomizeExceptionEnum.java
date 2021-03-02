package com.muke.common.enums;

/**
 * 自定义异常枚举类
 * @author 木可
 * @version 1.0
 * @date 2021/3/1 19:12
 */
public enum  CustomizeExceptionEnum {

    /**
     * 10:表示系统相关异常
     */
    ENTITY_VALID_EX(10001, "实体校验异常"),
    NOT_FOUND_DATA(10002, "数据未查询到");
    /**
     * 11：表示商品服务异常
     */


    /**
     * 12：表示订单服务异常
     */

    private final Integer code;
    private final String msg;

    private CustomizeExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
