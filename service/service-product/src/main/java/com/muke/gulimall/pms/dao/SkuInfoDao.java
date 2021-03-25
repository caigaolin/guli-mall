package com.muke.gulimall.pms.dao;

import com.muke.gulimall.pms.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muke.gulimall.pms.vo.web.ItemSkuInfoVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku信息
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {

    List<ItemSkuInfoVo.SkuItemSaleAttr> getItemSkuSaleAttr(Long spuId);
}
