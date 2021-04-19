package com.muke.gulimall.sls.controller;

import com.muke.common.utils.R;
import com.muke.gulimall.sls.dto.SeckillSkuRedisDTO;
import com.muke.gulimall.sls.service.SeckillService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/17 14:55
 */
@RequestMapping("/sls")
@Controller
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    /**
     * 获取当前上架的秒杀信息
     * @return
     */
    @ResponseBody
    @GetMapping("/seckill-info")
    public List<SeckillSkuRedisDTO> getCurrentSeckillInfo() {
        return  seckillService.getCurrentSeckillInfo();
    }

    /**
     * 通过skuId获取秒杀信息
     * @param skuId
     * @return
     */
    @ResponseBody
    @GetMapping("/seckill-info/{skuId}")
    public R getSeckillInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisDTO seckillSkuRedisDTO = seckillService.getSeckillInfoBySkuId(skuId);
        return R.ok().put("seckillInfo", seckillSkuRedisDTO);
    }

    /**
     * 商品秒杀
     * @param key
     * @param code
     * @param num
     * @return
     */
    @GetMapping("/kill")
    public String kill(@RequestParam("key") String key,
                       @RequestParam("code") String code,
                       @RequestParam("num") Integer num,
                       Model model) {
        String orderSn = seckillService.killProduct(key, code, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
