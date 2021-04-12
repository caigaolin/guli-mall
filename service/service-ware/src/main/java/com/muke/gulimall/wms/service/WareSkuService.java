package com.muke.gulimall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.to.SkuStockStatusTo;
import com.muke.common.to.mq.OrderTo;
import com.muke.common.to.mq.WareLockTo;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.wms.dto.WareLockDTO;
import com.muke.gulimall.wms.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageCondition(Map<String, Object> params);

    void addStore(Long skuId, Long wareId, Integer skuNum);

    List<SkuStockStatusTo> selectSkuStockStatus(List<Long> skuIds);

    Boolean lockWare(WareLockDTO wareLockDTO);

    void unLockWareStock(WareLockTo wareLockTo);

    void orderClosedReleaseStock(OrderTo orderTo);
}

