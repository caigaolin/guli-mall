package com.muke.gulimall.pms.dao;

import com.muke.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muke.gulimall.pms.vo.AttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchAttrgroup(@Param("vos") List<AttrGroupVo> deleteVos);
}
