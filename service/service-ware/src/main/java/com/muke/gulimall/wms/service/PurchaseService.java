package com.muke.gulimall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.wms.entity.PurchaseEntity;
import com.muke.gulimall.wms.vo.PurchaseDoneVo;
import com.muke.gulimall.wms.vo.PurchaseMergeVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageCondition(Map<String, Object> params);

    List<PurchaseEntity> getUnReceivePurchase();

    void mergePurchase(PurchaseMergeVo mergeVo);

    void receivedPurchase(List<Long> purchaseIds);

    void donePurchase(PurchaseDoneVo doneVo);
}

