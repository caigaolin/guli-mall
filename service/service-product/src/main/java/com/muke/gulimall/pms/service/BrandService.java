package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateBrandRelation(BrandEntity brand);

    PageUtils queryBrandPage(Map<String, Object> params);
}

