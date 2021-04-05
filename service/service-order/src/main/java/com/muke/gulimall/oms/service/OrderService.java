package com.muke.gulimall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.oms.entity.OrderEntity;
import com.muke.gulimall.oms.vo.OrderGenerateVo;
import com.muke.gulimall.oms.vo.OrderInfoVo;
import com.muke.gulimall.oms.vo.OrderRespVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 11:58:16
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 生成订单
     * @return
     */
    OrderInfoVo tradeOrder() throws ExecutionException, InterruptedException;

    OrderRespVo generateOrder(OrderGenerateVo orderVo, OrderRespVo respVo);
}

