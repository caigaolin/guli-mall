package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.AttrGroupEntity;
import com.muke.gulimall.pms.vo.AttrGroupWithAttrVo;
import com.muke.gulimall.pms.vo.web.ItemSkuInfoVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils pageQueryList(Map<String, Object> params, Long catId);

    Long[] getCompleteCateId(Long attrGroupId);

    List<AttrGroupWithAttrVo> getCategoryAttrGroupWithAttr(Long catId);

    List<ItemSkuInfoVo.SpuItemBaseAttr> getItemSpuBaseAttr(Long spuId, Long catalogId);
}

