package com.muke.gulimall.wms.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import com.muke.gulimall.wms.vo.MemberReceiveAddressRespVo;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.wms.entity.WareInfoEntity;
import com.muke.gulimall.wms.service.WareInfoService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;



/**
 * 仓库信息
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    /**
     * 查询运费信息
     * @param addrId 收货地址id
     * @return R
     */
    @GetMapping("/fare/{addrId}")
    public R getFare(@PathVariable("addrId") Long addrId) {
        MemberReceiveAddressRespVo memberAddress = wareInfoService.getFare(addrId);
        return R.ok().put("data", memberAddress);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("wms:wareinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("wms:wareinfo:info")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("wms:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("wms:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("wms:wareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
