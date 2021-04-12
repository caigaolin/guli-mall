package com.muke.gulimall.oms.dao;

import com.muke.common.enums.OrderStatusEnum;
import com.muke.gulimall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 11:58:16
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void updateOrderStatusByOrderSn(@Param("out_trade_no") String out_trade_no, @Param("code") Integer code);
}
