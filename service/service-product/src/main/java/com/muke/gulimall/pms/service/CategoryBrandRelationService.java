package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.BrandEntity;
import com.muke.gulimall.pms.entity.CategoryBrandRelationEntity;
import com.muke.gulimall.pms.vo.BrandRepsVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBrandCateRelation(CategoryBrandRelationEntity categoryBrandRelation);

    void updateRelationData(Long brandId, String name);

    void updateCateRelationData(Long catId, String name);

    List<BrandRepsVo> getCategoryRelationBrands(Long catId);
}

