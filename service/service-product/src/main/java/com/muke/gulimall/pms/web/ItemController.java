package com.muke.gulimall.pms.web;

import com.muke.gulimall.pms.service.SkuInfoService;
import com.muke.gulimall.pms.vo.web.ItemSkuInfoVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/15 18:49
 */
@Controller
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String toItemPage(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        ItemSkuInfoVo infoVo = skuInfoService.getItemSkuInfo(skuId);
        model.addAttribute("item", infoVo);
        return "item";
    }

}
