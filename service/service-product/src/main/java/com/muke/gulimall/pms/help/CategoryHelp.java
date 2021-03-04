package com.muke.gulimall.pms.help;

import com.muke.gulimall.pms.entity.CategoryEntity;
import com.muke.gulimall.pms.service.CategoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Resource
    private CategoryService categoryService;

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

    /**
     * 递归：查询完整的分类id
     * @param catId 下层分类id
     */
    public void getCompleteCateId(Long catId, List<Long> longs) {
        // 将本次的分类id加入集合中
        longs.add(catId);
        CategoryEntity categoryEntity = categoryService.getById(catId);
        if (categoryEntity.getParentCid() != 0) {
            // 说明该分类存在父级分类，则递归查找
            getCompleteCateId(categoryEntity.getParentCid(), longs);
        }
    }
}
