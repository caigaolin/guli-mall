package com.muke.gulimall.cart.controller;

import com.muke.gulimall.cart.service.CartService;
import com.muke.gulimall.cart.vo.CartItemVo;
import com.muke.gulimall.cart.vo.CartVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/21 11:36
 */
@Controller
public class CartController {

    @Resource
    private CartService cartService;

    /**
     * 购物车列表
     * @return
     */
    @GetMapping("/cartList.html")
    public String toCartList(Model model) throws ExecutionException, InterruptedException {
        CartVo cartVo = cartService.getCartList();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    /**
     * 加入购物车
     * @return
     */
    @GetMapping("/toAddCart")
    public String toAddCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addGoodsToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/toAddCartSuccess";
    }

    /**
     * 商品加入购物车成功
     * @return
     */
    @GetMapping("/toAddCartSuccess")
    public String toAddCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVo itemVo = cartService.getCartItem(skuId);
        model.addAttribute("skuInfo", itemVo);
        return "success";
    }

    /**
     * 修改商品选中状态
     * @param skuId
     * @param isCheck
     * @return
     */
    @GetMapping("/toCheck")
    public String toChangeCartItemCheck(@RequestParam("skuId") Long skuId,
                                        @RequestParam("isCheck") Boolean isCheck) {
        cartService.toChangeCartItemCheck(skuId, isCheck);
        return "redirect:http://cart.gulimall.com/cartList.html";
    }

    /**
     * 修改商品数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/toCount")
    public String toChangeCartItemCount(@RequestParam("skuId") Long skuId,
                                        @RequestParam("num") Integer num) {
        cartService.toChangeCartItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cartList.html";
    }

    /**
     * 删除购物车中商品项
     * @param skuId
     * @return
     */
    @GetMapping("/delete/{skuId}")
    public String removeCartItem(@PathVariable("skuId") Long skuId) {
        cartService.removeCartItem(skuId);
        return "redirect:http://cart.gulimall.com/cartList.html";
    }

    /**
     * 获取购物项
     * @return
     */
    @GetMapping("/get/cartItems")
    @ResponseBody
    public List<CartItemVo> getCartItemsByKey() {
        return cartService.getCartItemsByKey();
    }
}
