package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.AttrEntity;
import com.muke.gulimall.pms.vo.AttrRespVo;
import com.muke.gulimall.pms.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attrVo);

    PageUtils queryPageAttrRelation(Map<String, Object> params, Long catId, String type);

    AttrRespVo getAttrRelation(Long attrId);

    void updateAttrRelation(AttrVo attrVo);

    List<AttrEntity> queryPageAttrgroupRelationAttr(Long groupId);

    PageUtils getAttrgroupNoAttrRelation(Map<String, Object> params, Long groupId);
}

