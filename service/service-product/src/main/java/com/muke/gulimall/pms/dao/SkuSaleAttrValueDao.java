package com.muke.gulimall.pms.dao;

import com.muke.gulimall.pms.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<String> getAttrNameAndValue(@Param("skuId") Long skuId);
}
