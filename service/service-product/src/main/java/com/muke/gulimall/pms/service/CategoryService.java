package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取树形分类数据
     * @return List<CategoryEntity>
     */
    List<CategoryEntity> getCategoryTree();

    /**
     * 删除分类
     * @param catIds 分类id数组
     */
    void deleteBatchCate(Long[] catIds);
}

