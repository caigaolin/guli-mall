package com.muke.gulimall.oms.dto;

import com.muke.gulimall.oms.entity.OrderEntity;
import com.muke.gulimall.oms.entity.OrderItemEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/4 10:59
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private OrderEntity orderEntity;

    private List<OrderItemEntity> orderItems;
}
