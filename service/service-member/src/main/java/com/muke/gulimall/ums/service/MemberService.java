package com.muke.gulimall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.ums.entity.MemberEntity;
import com.muke.gulimall.ums.vo.LoginVo;
import com.muke.gulimall.ums.vo.RegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:20:08
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void registerMember(RegisterVo registerVo);

    MemberEntity loginMember(LoginVo loginVo);
}

