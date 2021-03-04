package com.muke.gulimall.pms.controller;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.muke.gulimall.pms.help.CategoryHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.pms.entity.CategoryEntity;
import com.muke.gulimall.pms.service.CategoryService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品三级分类
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
@RestController
@RequestMapping("pms/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 批量修改分类
     * @param entities 分类实体
     * @return R
     */
    @PutMapping("/update/batch")
    public R updateBatchCategory(@RequestBody CategoryEntity[] entities) {
        categoryService.updateBatchById(Arrays.asList(entities));
        return R.ok();
    }

    /**
     * 获取三级分类树形结构列表数据
     */
    @GetMapping("/list/tree")
    //@RequiresPermissions("pms:category:list")
    public R list(){
        List<CategoryEntity> treeCategory = categoryService.getCategoryTree();
        return R.ok().put("categories", treeCategory);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{catId}")
    //@RequiresPermissions("pms:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("pms:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    //@RequiresPermissions("pms:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateCateRelation(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    //@RequiresPermissions("pms:category:delete")
    public R delete(@RequestBody Long[] catIds){
        categoryService.deleteBatchCate(catIds);
        return R.ok();
    }

}
