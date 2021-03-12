package com.muke.gulimall.pms.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.muke.gulimall.pms.entity.ProductAttrValueEntity;
import com.muke.gulimall.pms.vo.AttrRespVo;
import com.muke.gulimall.pms.vo.AttrVo;
import com.muke.gulimall.pms.vo.SpuBaseAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.muke.gulimall.pms.service.AttrService;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.R;



/**
 * 商品属性
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
@RestController
@RequestMapping("pms/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 修改spu规则参数
     * @param spuId
     * @param baseAttrVos 修改参数实体
     * @return R
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuBaseAttr(@PathVariable("spuId") Long spuId,
                               @RequestBody List<SpuBaseAttrVo> baseAttrVos) {
        attrService.updateSpuBaseAttr(spuId, baseAttrVos);
        return R.ok();
    }

    /**
     * 获取spu规则参数
     * @return R
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R getSpuBaseAttr(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entities = attrService.listBaseAttr(spuId);
        return R.ok().put("data", entities);
    }

    /**
     * 条件分页查询属性及关联数据
     */
    @RequestMapping("/{attrType}/list/{catId}")
    //@RequiresPermissions("pms:attr:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catId") Long catId,
                  @PathVariable("attrType") String type){
        PageUtils page = attrService.queryPageAttrRelation(params, catId, type);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("pms:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo attr = attrService.getAttrRelation(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("pms:attr:save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("pms:attr:update")
    public R update(@RequestBody AttrVo attrVo){
		attrService.updateAttrRelation(attrVo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("pms:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
