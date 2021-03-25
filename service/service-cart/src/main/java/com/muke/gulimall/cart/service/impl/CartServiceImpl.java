package com.muke.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.common.constant.CartConstant;
import com.muke.common.utils.R;
import com.muke.gulimall.cart.feign.ProductFeign;
import com.muke.gulimall.cart.interceptor.CartInterceptor;
import com.muke.gulimall.cart.service.CartService;
import com.muke.gulimall.cart.vo.CartItemVo;
import com.muke.gulimall.cart.vo.CartSkuInfoVo;
import com.muke.gulimall.cart.vo.CartVo;
import com.muke.gulimall.cart.vo.MemberInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/24 14:13
 */
@Service
public class CartServiceImpl implements CartService {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private ProductFeign productFeign;

    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 将商品加入购物车
     * @param skuId
     * @param num 商品数量
     * @return
     */
    @Override
    public CartItemVo addGoodsToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartByRedis = getUserCartByRedis();
        // 首先查看购物车中是否存在该商品
        Object skuObj = cartByRedis.get(skuId.toString());
        CartItemVo itemVo;
        if (skuObj == null) {
            // 向购物车中新增商品
            itemVo = new CartItemVo();
            // 使用异步编排，把需要远程调用的请求作为异步形式
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                // 远程调用商品服务，获取商品信息
                R info = productFeign.info(skuId);
                if (info.getCode().equals(0)) {
                    // 调用成功，获取sku商品信息
                    Object skuInfoObj = info.get("skuInfo");
                    CartSkuInfoVo skuInfo = JSON.parseObject(JSON.toJSONString(skuInfoObj), CartSkuInfoVo.class);
                    itemVo.setSkuId(skuId);
                    itemVo.setTitle(skuInfo.getSkuTitle());
                    itemVo.setChecked(true);
                    itemVo.setCount(num);
                    itemVo.setImage(skuInfo.getSkuDefaultImg());
                    itemVo.setPrice(skuInfo.getPrice());
                }
            }, executor);

            CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
                // 远程调用商品服务，获取销售属性信息
                List<String> attrNameAndValue = productFeign.getAttrNameAndValue(skuId);
                itemVo.setSkuAttrs(attrNameAndValue);
            }, executor);

            // 只有当两个异步都完成任务才能往下执行
            CompletableFuture.allOf(skuInfoFuture, saleAttrFuture).get();
        } else {
            // 存在该商品，则只修改商品数量
            itemVo = JSON.parseObject(skuObj.toString(), CartItemVo.class);
            itemVo.setCount(itemVo.getCount() + num);
        }
        cartByRedis.put(skuId.toString(), JSON.toJSONString(itemVo));
        return itemVo;
    }

    /**
     * 获取购物车中商品项
     * @param skuId
     * @return
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartByRedis = getUserCartByRedis();
        String cartItemStr = (String) cartByRedis.get(skuId.toString());
        return JSON.parseObject(cartItemStr, CartItemVo.class);
    }

    /**
     * 获取购物车列表
     * @return
     */
    @Override
    public CartVo getCartList() throws ExecutionException, InterruptedException {
        // 从threadLocal中获取用户信息
        MemberInfoVo memberInfoVo = CartInterceptor.threadLocal.get();
        // 判断用户是否登录
        if (memberInfoVo.getUserId() == null) {
            // 为临时用户
            String cartKey = CartConstant.USER_CART_REDIS_PREFIX + memberInfoVo.getUserKey();
            List<CartItemVo> itemVoList = getCartItemByKey(cartKey);
            CartVo cartVo = new CartVo();
            cartVo.setCartItemVoList(itemVoList);
            return cartVo;
        } else {
            // 为登录用户
            // 1.判断该登录用户是否有临时购物车
            String temCartKey = CartConstant.USER_CART_REDIS_PREFIX + memberInfoVo.getUserKey();
            List<CartItemVo> itemVoList = getCartItemByKey(temCartKey);
            if (itemVoList != null) {
                // 存在临时购物车，将临时购物车中的商品项，放到登录用户的购物车中
                for (CartItemVo itemVo : itemVoList) {
                    addGoodsToCart(itemVo.getSkuId(), itemVo.getCount());
                }
                // 清除临时购物车
                redisTemplate.delete(temCartKey);
            }
            // 2.获取登录用户购物车
            String cartKey = CartConstant.USER_CART_REDIS_PREFIX + memberInfoVo.getUserId();
            List<CartItemVo> cartItemList = getCartItemByKey(cartKey);
            CartVo cartVo = new CartVo();
            cartVo.setCartItemVoList(cartItemList);
            return cartVo;
        }
    }

    /**
     * 修改商品选中状态
     * @param skuId
     * @param isCheck
     */
    @Override
    public void toChangeCartItemCheck(Long skuId, Boolean isCheck) {
        BoundHashOperations<String, Object, Object> userCartByRedis = getUserCartByRedis();
        String cartItemStr = (String) userCartByRedis.get(skuId.toString());
        if (!StringUtils.isEmpty(cartItemStr)) {
            CartItemVo itemVo = JSON.parseObject(cartItemStr, CartItemVo.class);
            itemVo.setChecked(isCheck);
            userCartByRedis.put(skuId.toString(), JSON.toJSONString(itemVo));
        }
    }

    /**
     * 修改商品数量
     * @param skuId
     * @param num
     */
    @Override
    public void toChangeCartItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> userCartByRedis = getUserCartByRedis();
        String cartItemStr = (String) userCartByRedis.get(skuId.toString());
        if (!StringUtils.isEmpty(cartItemStr)) {
            CartItemVo itemVo = JSON.parseObject(cartItemStr, CartItemVo.class);
            itemVo.setCount(num);
            userCartByRedis.put(skuId.toString(), JSON.toJSONString(itemVo));
        }
    }

    /**
     * 删除购物车中商品项
     * @param skuId
     */
    @Override
    public void removeCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> userCartByRedis = getUserCartByRedis();
        userCartByRedis.delete(skuId.toString());
    }

    /**
     * 设置redis中购物车的key
     * @return
     */
    private BoundHashOperations<String, Object, Object> getUserCartByRedis() {
        // 从threadLocal中获取当前用户信息
        MemberInfoVo memberInfoVo = CartInterceptor.threadLocal.get();
        String cartKey = CartConstant.USER_CART_REDIS_PREFIX;
        // 判断用户类型
        if (!StringUtils.isEmpty(memberInfoVo.getUserId())) {
            // 登录用户,使用id标识
            cartKey += memberInfoVo.getUserId();
        } else {
            // 临时用户，使用key标识
            cartKey += memberInfoVo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    /**
     * 通过key获取购物车项
     * @param key
     * @return
     */
    private List<CartItemVo> getCartItemByKey(String key) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        // 获取临时用户的购物车
        List<Object> values = hashOps.values();
        if (!CollectionUtils.isEmpty(values)) {
            return values.stream().map(obj -> {
                String objStr = (String) obj;
                return JSON.parseObject(objStr, CartItemVo.class);
            }).collect(Collectors.toList());
        }
        return null;
    }
}
