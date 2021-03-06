package com.muke.gulimall.sms.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.muke.gulimall.sms.entity.MemberPriceEntity;
import com.muke.gulimall.sms.service.MemberPriceService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;


/**
 * 商品会员价格
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:23:44
 */
@RestController
@RequestMapping("sms/memberprice")
public class MemberPriceController {
    @Autowired
    private MemberPriceService memberPriceService;

    /**
     * 批量保存
     */
    @RequestMapping("/save/batch")
    //@RequiresPermissions("sms:memberprice:save")
    public R saveBatch(@RequestBody List<MemberPriceEntity> memberPrice) {
        List<MemberPriceEntity> memberPriceEntityList = memberPrice.stream().filter(
                memberPriceEntity -> memberPriceEntity.getMemberPrice().compareTo(new BigDecimal(0)) != 0
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntityList);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("sms:memberprice:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberPriceService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("sms:memberprice:info")
    public R info(@PathVariable("id") Long id) {
        MemberPriceEntity memberPrice = memberPriceService.getById(id);

        return R.ok().put("memberPrice", memberPrice);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("sms:memberprice:save")
    public R save(@RequestBody MemberPriceEntity memberPrice) {
        memberPriceService.save(memberPrice);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("sms:memberprice:update")
    public R update(@RequestBody MemberPriceEntity memberPrice) {
        memberPriceService.updateById(memberPrice);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("sms:memberprice:delete")
    public R delete(@RequestBody Long[] ids) {
        memberPriceService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
