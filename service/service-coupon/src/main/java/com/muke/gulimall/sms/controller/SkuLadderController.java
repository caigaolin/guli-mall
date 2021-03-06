package com.muke.gulimall.sms.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.sms.entity.SkuLadderEntity;
import com.muke.gulimall.sms.service.SkuLadderService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;



/**
 * 商品阶梯价格
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:23:44
 */
@RestController
@RequestMapping("sms/skuladder")
public class SkuLadderController {
    @Autowired
    private SkuLadderService skuLadderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("sms:skuladder:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuLadderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("sms:skuladder:info")
    public R info(@PathVariable("id") Long id){
		SkuLadderEntity skuLadder = skuLadderService.getById(id);

        return R.ok().put("skuLadder", skuLadder);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("sms:skuladder:save")
    public R save(@RequestBody SkuLadderEntity skuLadder){
        if (skuLadder.getFullCount() != 0 || skuLadder.getDiscount().compareTo(new BigDecimal(0)) != 0) {
            skuLadderService.save(skuLadder);
        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("sms:skuladder:update")
    public R update(@RequestBody SkuLadderEntity skuLadder){
		skuLadderService.updateById(skuLadder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("sms:skuladder:delete")
    public R delete(@RequestBody Long[] ids){
		skuLadderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
