package com.muke.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/18 10:57
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderTo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 场次id
     */
    private Long sessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 会员id
     */
    private Long memberId;
    /**
     * 秒杀数量
     */
    private Integer num;
    /**
     * 秒杀价格
     */
    private BigDecimal killPrice;
}
