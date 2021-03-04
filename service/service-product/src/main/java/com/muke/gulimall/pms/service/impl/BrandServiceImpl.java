package com.muke.gulimall.pms.service.impl;

import com.muke.gulimall.pms.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.BrandDao;
import com.muke.gulimall.pms.entity.BrandEntity;
import com.muke.gulimall.pms.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 条件查询品牌分页数据
     * @param params 分页参数
     * @return PageUtils
     */
    @Override
    public PageUtils queryBrandPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> {
               obj.eq("brand_id", key).or().like("name", key);
            });
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 修改品牌及关联数据
     * @param brand 品牌实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBrandRelation(BrandEntity brand) {
        // 修改品牌数据
        baseMapper.updateById(brand);
        // 判断是否修改名称
        if (!StringUtils.isEmpty(brand.getName())) {
            // 则需要修改关联数据
            categoryBrandRelationService.updateRelationData(brand.getBrandId(), brand.getName());
        }
    }
}