package com.muke.gulimall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;
import com.muke.gulimall.pms.dao.AttrGroupDao;
import com.muke.gulimall.pms.entity.AttrAttrgroupRelationEntity;
import com.muke.gulimall.pms.entity.AttrEntity;
import com.muke.gulimall.pms.entity.AttrGroupEntity;
import com.muke.gulimall.pms.help.CategoryHelp;
import com.muke.gulimall.pms.service.AttrAttrgroupRelationService;
import com.muke.gulimall.pms.service.AttrGroupService;
import com.muke.gulimall.pms.service.AttrService;
import com.muke.gulimall.pms.vo.AttrGroupWithAttrVo;
import com.muke.gulimall.pms.vo.web.ItemSkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private CategoryHelp categoryHelp;

    @Resource(name = "attrAttrgroupRelationService")
    private AttrAttrgroupRelationService relationService;

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    /**
     * 分页条件查询
     * @param params 分页参数
     * @param catId 分类id
     * @return PageUtils
     */
    @Override
    public PageUtils pageQueryList(Map<String, Object> params, Long catId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        // 判断cateId
        if (catId != 0) {
            wrapper.eq("catelog_id", catId);
        }
        // 判断是否包含检索字段
        String key = (String) params.get("key");
        // 判断非空
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    /**
     * 获取完整的分类id
     * @param attrGroupId 下层分类id
     * @return Long[]
     */
    @Override
    public Long[] getCompleteCateId(Long attrGroupId) {
        List<Long> longs = new ArrayList<>();
        categoryHelp.getCompleteCateId(attrGroupId, longs);
        // 对集合进行反转
        Collections.reverse(longs);
        return longs.toArray(new Long[longs.size()]);
    }

    /**
     * 获取分类下的所有分组及关联属性
     * @param catId 分类id
     * @return List<AttrGroupWithAttrVo>
     */
    @Override
    public List<AttrGroupWithAttrVo> getCategoryAttrGroupWithAttr(Long catId) {
        // 获取分类下的所有分组
        List<AttrGroupEntity> groupEntityList = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
        return groupEntityList.stream().map(group -> {
            // 构建最终返回对象
            AttrGroupWithAttrVo withAttrVo = new AttrGroupWithAttrVo();

            List<AttrAttrgroupRelationEntity> relationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().select("attr_id").eq("attr_group_id", group.getAttrGroupId()));
            // 得到所有属性id集合
            List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            // 将属性集合默认设置为空
            withAttrVo.setAttrs(new ArrayList<>());
            // 判断属性id集合是否为空
            if (!CollectionUtils.isEmpty(attrIds)) {
                // 得到所有属性
                List<AttrEntity> attrEntityList = attrService.listByIds(attrIds);
                withAttrVo.setAttrs(attrEntityList);
            }
            BeanUtils.copyProperties(group, withAttrVo);
            return withAttrVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemSkuInfoVo.SpuItemBaseAttr> getItemSpuBaseAttr(Long spuId, Long catalogId) {
        return baseMapper.getItemSpuBaseAttr(spuId, catalogId);
    }

}