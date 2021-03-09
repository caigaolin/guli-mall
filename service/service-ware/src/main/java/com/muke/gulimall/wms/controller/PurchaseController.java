package com.muke.gulimall.wms.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.muke.gulimall.wms.vo.PurchaseDoneVo;
import com.muke.gulimall.wms.vo.PurchaseMergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.wms.entity.PurchaseEntity;
import com.muke.gulimall.wms.service.PurchaseService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;



/**
 * 采购信息
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购
     * @param doneVo 采购完成实体
     * @return R
     */
    @PostMapping("/done")
    public R donePurchase(@RequestBody PurchaseDoneVo doneVo) {
        purchaseService.donePurchase(doneVo);
        return R.ok();
    }


    /**
     * 领取采购单
     * @param purchaseIds 采购单id
     * @return R
     */
    @PostMapping("/received")
    public R receivedPurchase(@RequestBody List<Long> purchaseIds) {
        purchaseService.receivedPurchase(purchaseIds);
        return R.ok();
    }

    /**
     * 合并采购需求
     * @param mergeVo 合并实体
     * @return R
     */
    @PostMapping("/merge")
    public R merge(@RequestBody PurchaseMergeVo mergeVo) {
        purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }

    /**
     * 查询未领取的采购单信息
     * @return R
     */
    @GetMapping("/unreceive/list")
    public R getUnReceivePurchase() {
        List<PurchaseEntity> purchases = purchaseService.getUnReceivePurchase();
        return R.ok().put("data", purchases);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("wms:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("wms:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("wms:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("wms:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("wms:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
