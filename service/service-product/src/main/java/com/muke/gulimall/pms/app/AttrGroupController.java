package com.muke.gulimall.pms.app;

import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;
import com.muke.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.muke.gulimall.pms.entity.AttrEntity;
import com.muke.gulimall.pms.entity.AttrGroupEntity;
import com.muke.gulimall.pms.service.AttrAttrgroupRelationService;
import com.muke.gulimall.pms.service.AttrGroupService;
import com.muke.gulimall.pms.service.AttrService;
import com.muke.gulimall.pms.vo.AttrGroupVo;
import com.muke.gulimall.pms.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 属性分组
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private AttrService attrService;

    @Resource(name = "attrAttrgroupRelationService")
    private AttrAttrgroupRelationService relationService;

    ///product/attrgroup/{catelogId}/withattr

    /**
     * 获取分类下所有分组&关联属性
     * @param catId 分类id
     * @return R
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupRelationWithAttr(@PathVariable("catelogId") Long catId) {
        List<AttrGroupWithAttrVo> attrVos = attrGroupService.getCategoryAttrGroupWithAttr(catId);

        return R.ok().put("data", attrVos);
    }

    /**
     * 保存属性分组关联关系
     * @return R
     */
    @PostMapping("/attr/relation")
    public R saveAttrgroupRelation(@RequestBody List<AttrGroupVo> attrGroupVos) {
        List<AttrAttrgroupRelationEntity> relationEntityList = attrGroupVos.stream().map(attrGroupVo -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrSort(0);
            relationEntity.setAttrId(attrGroupVo.getAttrId());
            relationEntity.setAttrGroupId(attrGroupVo.getAttrGroupId());
            return relationEntity;
        }).collect(Collectors.toList());

        relationService.saveBatch(relationEntityList);
        return R.ok();
    }

    /**
     * 查询属性分组中没有被关联的属性
     * @param groupId 分组id
     * @return R
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getAttrgroupNoAttrRelation(@RequestParam Map<String, Object> params,
                                        @PathVariable("attrgroupId") Long groupId) {
        PageUtils page = attrService.getAttrgroupNoAttrRelation(params, groupId);

        return R.ok().put("page", page);
    }

    /**
     * 删除属性分组的关联关系
     * @param deleteVos 删除实体
     * @return R
     */
    @PostMapping("/attr/relation/delete")
    public R deleteAttrgroupRelation(@RequestBody List<AttrGroupVo> deleteVos) {
        relationService.deleteAttrgroupRelation(deleteVos);
        return R.ok();
    }

    /**
     * 查询属性分组下关联的属性
     * @param groupId 分组id
     * @return R
     */
    @GetMapping("/{attrgorupId}/attr/relation")
    public R getAttrgroupRelationListPage(@PathVariable("attrgorupId") Long groupId) {
        List<AttrEntity> list = attrService.queryPageAttrgroupRelationAttr(groupId);

        return R.ok().put("data", list);
    }

    /**
     * 分页条件查询
     */
    @RequestMapping("/list/{categoryId}")
    //@RequiresPermissions("pms:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long catId){
        PageUtils page = attrGroupService.pageQueryList(params, catId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("pms:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long[] catelogIds = attrGroupService.getCompleteCateId(attrGroup.getCatelogId());
        attrGroup.setCatelogIds(catelogIds);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("pms:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("pms:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("pms:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
