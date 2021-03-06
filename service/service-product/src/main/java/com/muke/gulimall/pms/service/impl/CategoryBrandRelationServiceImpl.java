package com.muke.gulimall.pms.service.impl;

import com.muke.gulimall.pms.entity.BrandEntity;
import com.muke.gulimall.pms.entity.CategoryEntity;
import com.muke.gulimall.pms.service.BrandService;
import com.muke.gulimall.pms.service.CategoryService;
import com.muke.gulimall.pms.vo.BrandRepsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.CategoryBrandRelationDao;
import com.muke.gulimall.pms.entity.CategoryBrandRelationEntity;
import com.muke.gulimall.pms.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private BrandService brandService;

    @Resource
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存品牌分类的关联数据
     * @param categoryBrandRelation 关联实体
     */
    @Override
    public void saveBrandCateRelation(CategoryBrandRelationEntity categoryBrandRelation) {
        // 查询品牌
        BrandEntity brandEntity = brandService.getById(categoryBrandRelation.getBrandId());
        // 查询分类
        CategoryEntity categoryEntity = categoryService.getById(categoryBrandRelation.getCatelogId());

        // 保存关联数据
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        baseMapper.insert(categoryBrandRelation);
    }

    /**
     * 修改关联品牌name
     * @param brandId 品牌id
     * @param name 品牌name
     */
    @Override
    public void updateRelationData(Long brandId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setBrandId(brandId);
        entity.setBrandName(name);
        baseMapper.update(entity, new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    /**
     * 修改关联分类name
     * @param catId 分类id
     * @param name 分类name
     */
    @Override
    public void updateCateRelationData(Long catId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setCatelogId(catId);
        entity.setCatelogName(name);
        baseMapper.update(entity, new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }

    /**
     * 通过分类获取品牌
     * @param catId 分类id
     * @return List<BrandEntity>
     */
    @Override
    public List<BrandRepsVo> getCategoryRelationBrands(Long catId) {
        List<CategoryBrandRelationEntity> relationEntities = baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        return relationEntities.stream().map(item -> {
            BrandRepsVo brandRepsVo = new BrandRepsVo();
            BeanUtils.copyProperties(item, brandRepsVo);
            return brandRepsVo;
        }).collect(Collectors.toList());

    }
}