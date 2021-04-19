package com.muke.gulimall.sms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.muke.gulimall.sms.dto.Recent3DaysSessionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.sms.entity.SeckillSessionEntity;
import com.muke.gulimall.sms.service.SeckillSessionService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;



/**
 * 秒杀活动场次
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:23:44
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {
    @Autowired
    private SeckillSessionService seckillSessionService;

    /**
     * 获取最近3天的活动信息
     * @return
     */
    @GetMapping("/recent-3days/session")
    public R getRecent3DaysSession() {
        List<Recent3DaysSessionDTO> recentList =  seckillSessionService.getRecent3DaysSession();
        return R.ok().put("recentList", recentList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("sms:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("sms:seckillsession:info")
    public R info(@PathVariable("id") Long id){
		SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("sms:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("sms:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("sms:seckillsession:delete")
    public R delete(@RequestBody Long[] ids){
		seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
