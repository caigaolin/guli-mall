package com.muke.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车
 * @author 木可
 * @version 1.0
 * @date 2021/3/22 21:45
 */

public class CartVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车中的购物项列表
     */
    List<CartItemVo> cartItemVoList;

    /**
     * 商品类型数量
     */
    private Integer typeCount;

    /**
     * 已选中数量
     */
    private Integer checkedCount;

    /**
     * 已选中商品总价
     */
    private BigDecimal checkedTotalPrice;

    /**
     * 已选中商品减免价格
     */
    private BigDecimal checkedReducePrice;

    public Integer getTypeCount() {
        int count = 0;
        if (!CollectionUtils.isEmpty(cartItemVoList)) {
            for (CartItemVo cartItemVo : cartItemVoList) {
                if (cartItemVo.getChecked()) {
                    count++;
                }
            }
        }
        typeCount = count;
        return typeCount;
    }

    public Integer getCheckedCount() {
        int count = 0;
        if (!CollectionUtils.isEmpty(cartItemVoList)) {
            for (CartItemVo cartItemVo : cartItemVoList) {
                if (cartItemVo.getChecked()) {
                    count += cartItemVo.getCount();
                }
            }
        }
        checkedCount = count;
        return checkedCount;
    }

    /**
     * 总价=选中商品项总价 - 选中商品项优惠价
     * @return
     */
    public BigDecimal getCheckedTotalPrice() {
        // 总价
        BigDecimal totalPrice = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(cartItemVoList)) {
            for (CartItemVo cartItemVo : cartItemVoList) {
                if (cartItemVo.getChecked()) {
                    totalPrice = totalPrice.add(cartItemVo.getTotalPrice());
                }
            }
        }
        checkedTotalPrice = totalPrice.subtract(getCheckedReducePrice());
        return checkedTotalPrice;
    }

    public BigDecimal getCheckedReducePrice() {
        BigDecimal totalReduce = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(cartItemVoList)) {
            for (CartItemVo cartItemVo : cartItemVoList) {
                if (cartItemVo.getChecked()) {
                    totalReduce = totalReduce.add(cartItemVo.getAmount());
                }
            }
        }
        checkedReducePrice = totalReduce;
        return checkedReducePrice;
    }

    public List<CartItemVo> getCartItemVoList() {
        return cartItemVoList;
    }

    public void setCartItemVoList(List<CartItemVo> cartItemVoList) {
        this.cartItemVoList = cartItemVoList;
    }

    @Override
    public String toString() {
        return "CartVo{" +
                "cartItemVoList=" + cartItemVoList +
                ", typeCount=" + typeCount +
                ", checkedCount=" + checkedCount +
                ", checkedTotalPrice=" + checkedTotalPrice +
                ", checkedReducePrice=" + checkedReducePrice +
                '}';
    }
}
