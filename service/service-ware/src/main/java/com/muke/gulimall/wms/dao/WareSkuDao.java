package com.muke.gulimall.wms.dao;

import com.muke.gulimall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 12:15:39
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateStore(@Param("id") Long id, @Param("skuNum") Integer skuNum);
}
