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
    NOT_FOUND_DATA(10002, "数据未查询到"),
    /**
     * 11：表示商品服务异常
     */


    /**
     * 12：表示订单服务异常
     */
    SAVE_ORDER_EX(12000, "保存订单失败"),
    SAVE_ORDER_ITEM_EX(12001, "保存订单项失败"),
    /**
     * 13：表示优惠服务异常
     */
    SAVE_BOUNDS_FAIL(13001, "保存spu优惠价格信息失败"),
    SAVE_LADDER_FAIL(13002, "保存sku阶梯价格失败"),
    SAVE_FULL_REDUCTION_FAIL(13003, "保存sku满减信息失败"),
    SAVE_MEMBER_PRICE_FAIL(13004, "保存sku会员价信息失败"),

    /**
     * 14:第三方服务异常
     */
    SEND_CODE_EX(14001, "发送短信验证码异常"),
    SEND_CODE_OFTEN(14002, "请勿重复发送验证码"),

    /**
     * 15:用户服务
     */
    USERNAME_EXITS(15001, "用户名已存在"),
    PHONE_EXITS(15002, "手机号已存在"),
    ACCOUNT_PASSWORD_ERROR(15003, "账号密码错误"),

    /**
     * 16:库存服务
     */
    LOCK_STOCK_EX(16000, "锁定库存失败");

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
