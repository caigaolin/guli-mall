package com.muke.gulimall.sms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muke.gulimall.sms.entity.CouponEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:23:44
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
