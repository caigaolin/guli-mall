package com.muke.gulimall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.wms.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageCondition(Map<String, Object> params);
}

