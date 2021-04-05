package com.muke.gulimall.wms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.to.SkuStockStatusTo;
import com.muke.gulimall.wms.dto.WareLockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.wms.entity.WareSkuEntity;
import com.muke.gulimall.wms.service.WareSkuService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;



/**
 * 商品库存
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 锁定库存
     * @param wareLockDTOS
     * @return
     */
    @PostMapping("/lock")
    public R lockWare(@RequestBody List<WareLockDTO> wareLockDTOS) {
        Boolean isLock = wareSkuService.lockWare(wareLockDTOS);
        if (isLock) {
            return R.ok().put("isLock", isLock);
        }
        return R.error(CustomizeExceptionEnum.LOCK_STOCK_EX);
    }

    /**
     * 检查商品是否存在库存
     * @param skuIds skuId集合
     * @return List<SkuStockStatusTo>
     */
    @PostMapping("/stock/status")
    public List<SkuStockStatusTo> getSkuStockStatus(@RequestBody List<Long> skuIds) {
        return wareSkuService.selectSkuStockStatus(skuIds);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("wms:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPageCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("wms:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("wms:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("wms:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("wms:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
