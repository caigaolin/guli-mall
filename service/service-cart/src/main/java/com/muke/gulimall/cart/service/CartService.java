package com.muke.gulimall.cart.service;

import com.muke.gulimall.cart.vo.CartItemVo;
import com.muke.gulimall.cart.vo.CartVo;
import com.muke.gulimall.cart.vo.MemberInfoVo;

import java.util.concurrent.ExecutionException;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/24 14:13
 */
public interface CartService {
    /**
     * 添加商品放入购物车
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItemVo addGoodsToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中商品项
     * @param skuId
     * @return
     */
    CartItemVo getCartItem(Long skuId);

    /**
     * 获取购物车列表
     * @return
     */
    CartVo getCartList() throws ExecutionException, InterruptedException;

    /**
     * 修改商品选中状态
     * @param skuId
     * @param isCheck
     */
    void toChangeCartItemCheck(Long skuId, Boolean isCheck);

    /**
     * 修改商品数量
     * @param skuId
     * @param num
     */
    void toChangeCartItemCount(Long skuId, Integer num);

    /**
     * 删除购物车中商品项
     * @param skuId
     */
    void removeCartItem(Long skuId);
}
