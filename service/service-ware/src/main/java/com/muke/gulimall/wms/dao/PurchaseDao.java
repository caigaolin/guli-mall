package com.muke.gulimall.wms.dao;

import com.muke.gulimall.wms.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
