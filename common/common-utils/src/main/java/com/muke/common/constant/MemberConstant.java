package com.muke.common.constant;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/18 12:36
 */
public class MemberConstant {

    /**
     * 会员默认等级枚举
     */
    public enum memberLevel {
        /**
         * 默认等级
         */
        DEFAULT(1),
        /**
         * 其它等级
         */
        OTHER(0);

        private final Integer code;

        memberLevel(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

    }
}
