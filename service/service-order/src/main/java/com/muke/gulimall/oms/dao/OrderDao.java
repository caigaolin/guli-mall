package com.muke.gulimall.oms.dao;

import com.muke.gulimall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 11:58:16
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
