package com.muke.gulimall.oms.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 15:23
 */
public class OrderInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Setter @Getter
    private List<OrderMemberAddressVo> memberAddressVos;
    @Setter @Getter
    private List<CartItemVo> orderItemVos;
    @Setter @Getter
    private Map<Long, Boolean> isStockMap;
    /**
     * 防重令牌
     */
    @Getter @Setter
    private String token;

    /**
     * 会员积分
     */
    @Setter @Getter
    private Integer integral;

    /**
     * 商品总金额
     */
    public BigDecimal getTotalPrice() {
        BigDecimal sum = new BigDecimal("0");
        for (CartItemVo orderItemVo : orderItemVos) {
            sum = sum.add(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())));
        }
        return sum;
    }

    /**
     * 应付金额
     */
    public BigDecimal getPayPrice() {
        return getTotalPrice().subtract(new BigDecimal(0));
    }

}
