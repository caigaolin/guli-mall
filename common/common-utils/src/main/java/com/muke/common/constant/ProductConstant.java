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

    /**
     * 属性检索状态枚举
     */
    public enum attrSearchStatus {
        /**
         * 可检索
         */
        CAN_INDEX(1),
        /**
         * 不可检索
         */
        NO_INDEX(0);

        private final Integer code;

        attrSearchStatus(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

    }

    /**
     * 商品上架状态枚举
     */
    public enum productPublishStatus {
        /**
         * 新建
         */
        NEW(0),
        /**
         * 上架
         */
        UP(1),
        /**
         * 下架
         */
        DOWN(2);

        private final Integer code;

        productPublishStatus(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

    }
}
