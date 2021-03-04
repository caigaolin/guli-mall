package com.muke.common.constant;

/**
 * 商品系统常量类
 * @author 木可
 * @version 1.0
 * @date 2021/3/4 12:38
 */
public class ProductConstant {

    /**
     * 属性类型枚举
     */
    public enum attrType {
        /**
         * 基本属性
         */
        BASE_TYPE(1, "base"),
        /**
         * 销售属性
         */
        SALE_TYPE(0, "sale");

        private final Integer code;
        private final String msg;

        attrType(Integer code, String msg) {
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
}
