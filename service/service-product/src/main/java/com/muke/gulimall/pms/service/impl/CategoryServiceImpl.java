package com.muke.gulimall.pms.service.impl;

import com.muke.gulimall.pms.help.CategoryHelp;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.CategoryDao;
import com.muke.gulimall.pms.entity.CategoryEntity;
import com.muke.gulimall.pms.service.CategoryService;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryHelp categoryHelp;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取树形分类数据
     * @return List<CategoryEntity>
     */
    @Override
    public List<CategoryEntity> getCategoryTree() {
        // 分类列表数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 获取树形分类数据
        return entities.stream()
                .filter(item -> item.getParentCid() == 0)
                .peek(item -> {
                    item.setChildren(categoryHelp.getCategoryChildren(item, entities));
                })
                .sorted(Comparator.comparingInt(cate -> (cate.getSort() == null ? 0 : cate.getSort())))
                .collect(Collectors.toList());
    }

    /**
     * 删除没有引用的分类
     * @param catIds 分类id数组
     */
    @Override
    public void deleteBatchCate(Long[] catIds) {
        // TODO 判断该分类是否有关联数据

        // 删除分类
        baseMapper.deleteBatchIds(Arrays.asList(catIds));
    }
}