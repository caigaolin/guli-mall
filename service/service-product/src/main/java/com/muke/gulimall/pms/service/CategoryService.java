package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.CategoryEntity;

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
}

