package com.muke.gulimall.pms.service.impl;

import com.muke.gulimall.pms.vo.AttrGroupVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.AttrAttrgroupRelationDao;
import com.muke.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.muke.gulimall.pms.service.AttrAttrgroupRelationService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


/**
 * @author 木可
 */
@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Resource
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 删除属性分组关联数据
     * @param deleteVos 删除实体
     */
    @Override
    public void deleteAttrgroupRelation(List<AttrGroupVo> deleteVos) {
        if (!CollectionUtils.isEmpty(deleteVos)) {
            relationDao.deleteBatchAttrgroup(deleteVos);
        }
    }
}