package com.muke.gulimall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.ums.entity.MemberReceiveAddressEntity;

import java.util.Map;

/**
 * 会员收货地址
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:20:08
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

