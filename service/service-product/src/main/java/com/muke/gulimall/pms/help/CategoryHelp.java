package com.muke.gulimall.pms.help;

import com.muke.gulimall.pms.entity.CategoryEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/2/27 11:07
 */
@Component
public class CategoryHelp {

    /**
     * 封装分类为树形结构
     * @param rootCategory 上级分类
     * @param entities 所有分类
     * @return List<CategoryEntity>
     */
    public List<CategoryEntity> getCategoryChildren(CategoryEntity rootCategory, List<CategoryEntity> entities) {
        return entities.stream()
                .filter(item -> item.getParentCid().equals(rootCategory.getCatId()))
                .peek(item -> {
                    item.setChildren(getCategoryChildren(item, entities));
                })
                .sorted(Comparator.comparingInt(cate -> (cate.getSort() == null ? 0 : cate.getSort())))
                .collect(Collectors.toList());
    }
}
