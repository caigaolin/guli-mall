package com.muke.gulimall.ums.controller;

import java.util.Arrays;
import java.util.Map;

import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.vo.MemberRespVo;
import com.muke.gulimall.ums.feign.CouponClient;
import com.muke.gulimall.ums.vo.LoginVo;
import com.muke.gulimall.ums.vo.RegisterVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.ums.entity.MemberEntity;
import com.muke.gulimall.ums.service.MemberService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;

import javax.annotation.Resource;


/**
 * 会员
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:20:08
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Resource
    private CouponClient couponClient;

    @PostMapping("/login")
    public R loginMember(@RequestBody LoginVo loginVo) {
        MemberEntity member = memberService.loginMember(loginVo);
        if (member == null) {
            return R.error(CustomizeExceptionEnum.ACCOUNT_PASSWORD_ERROR);
        }
        MemberRespVo memberRespVo = new MemberRespVo();
        BeanUtils.copyProperties(member, memberRespVo);
        return R.ok().put("member", memberRespVo);
    }

    @PostMapping("/register")
    public R registerMember(@RequestBody RegisterVo registerVo) {
        memberService.registerMember(registerVo);
        return R.ok();
    }

    @GetMapping("/coupons")
    public R getMember() {
        R couponByMember = couponClient.getCouponByMember();
        Object coupons = couponByMember.get("coupons");
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("李书琦");

        return R.ok().put("member", memberEntity).put("coupons", coupons);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ums:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ums:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ums:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ums:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ums:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
