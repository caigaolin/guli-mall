package com.muke.common.constant;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/6 16:05
 */
public class WareConstant {

    /**
     * 采购单状态
     */
    public enum PurchaseStatus {
        /**
         * 新建
         */
        CREATED(0),
        /**
         * 已分配
         */
        ALLOCATED(1),
        /**
         * 已领取
         */
        RECEIVED(2),
        /**
         * 已完成
         */
        FINISHED(3),
        /**
         * 有异常
         */
        HAS_EX(4);

        private final Integer code;

        PurchaseStatus(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }

    /**
     * 采购需求状态
     */
    public enum PurchaseDemandStatus {
        /**
         * 新建
         */
        CREATED(0),
        /**
         * 已分配
         */
        ALLOCATED(1),
        /**
         * 正在采购
         */
        PURCHASING(2),
        /**
         * 已完成
         */
        FINISHED(3),
        /**
         * 有异常
         */
        HAS_EX(4);

        private final Integer code;

        PurchaseDemandStatus(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

    }

    public enum WareLockStatus {
        /**
         * 已锁定
         */
        LOCKED(1),
        /**
         * 已解锁
         */
        UN_LOCK(2),
        /**
         * 已扣减
         */
        DEDUCTION(3);


        private final Integer code;

        WareLockStatus(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }
}
