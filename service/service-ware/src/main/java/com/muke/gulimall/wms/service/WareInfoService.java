package com.muke.gulimall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.wms.entity.WareInfoEntity;
import com.muke.gulimall.wms.vo.MemberReceiveAddressRespVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    MemberReceiveAddressRespVo getFare(Long addrId);
}

