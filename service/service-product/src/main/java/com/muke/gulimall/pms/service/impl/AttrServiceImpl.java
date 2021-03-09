package com.muke.gulimall.pms.service.impl;

import com.muke.common.constant.ProductConstant;
import com.muke.gulimall.pms.entity.*;
import com.muke.gulimall.pms.help.CategoryHelp;
import com.muke.gulimall.pms.service.*;
import com.muke.gulimall.pms.vo.AttrRespVo;
import com.muke.gulimall.pms.vo.AttrVo;
import com.muke.gulimall.pms.vo.SpuBaseAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.AttrDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource(name = "attrAttrgroupRelationService")
    private AttrAttrgroupRelationService attRelationService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private CategoryHelp categoryHelp;

    @Resource(name = "productAttrValueService")
    private ProductAttrValueService attrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存属性信息及关联信息
     *
     * @param attrVo 属性实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAttr(AttrVo attrVo) {
        // 保存属性数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        baseMapper.insert(attrEntity);

        // 判断是否为基础属性 并且 属性分组id不能为空
        if (attrVo.getAttrType().equals(ProductConstant.attrType.BASE_TYPE.getCode()) && !StringUtils.isEmpty(attrVo.getAttrGroupId())) {
            // 保存关联数据
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrId(attrEntity.getAttrId());
            entity.setAttrGroupId(attrVo.getAttrGroupId());
            entity.setAttrSort(0);
            attRelationService.save(entity);
        }

    }

    /**
     * 条件分页查询属性及关联数据
     *
     * @param params 分页条件
     * @param catId  分类id
     * @param type
     * @return PageUtils
     */
    @Override
    public PageUtils queryPageAttrRelation(Map<String, Object> params, Long catId, String type) {
        // 判断并获取属性类型
        boolean isBaseType = type.equalsIgnoreCase(ProductConstant.attrType.BASE_TYPE.getMsg());
        int code = isBaseType ? ProductConstant.attrType.BASE_TYPE.getCode() : ProductConstant.attrType.SALE_TYPE.getCode();
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type", code);

        if (catId != 0) {
            wrapper.eq("catelog_id", catId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        // 封装属性数据
        List<AttrRespVo> respVoList = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            if (isBaseType) {
                // 获取分组name
                AttrAttrgroupRelationEntity relationEntity = attRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (relationEntity != null) {
                    AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                    if (groupEntity != null) {
                        attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                    }

                }
            }

            // 获取分类name
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(respVoList);
        return pageUtils;
    }

    /**
     * 根据id查询属性信息和关联信息
     *
     * @param attrId 属性id
     * @return AttrRespVo
     */
    @Override
    public AttrRespVo getAttrRelation(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        // 查询属性基本信息
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        if (attrEntity.getAttrType().equals(ProductConstant.attrType.BASE_TYPE.getCode())) {
            // 查询属性分组信息
            AttrAttrgroupRelationEntity relationEntity = attRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
        }

        // 查询分类信息
        List<Long> longs = new ArrayList<>();
        categoryHelp.getCompleteCateId(attrEntity.getCatelogId(), longs);
        Collections.reverse(longs);
        attrRespVo.setCatelogPath(longs.toArray(new Long[longs.size()]));

        return attrRespVo;
    }

    /**
     * 修改属性数据及关联数据
     *
     * @param attrVo 属性实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttrRelation(AttrVo attrVo) {
        // 修改属性基本数据
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, entity);
        baseMapper.updateById(entity);

        // 修改属性的关联信息
        if (attrVo.getAttrType().equals(ProductConstant.attrType.BASE_TYPE.getCode()) && !StringUtils.isEmpty(attrVo.getAttrGroupId())) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrVo.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            // 获取中间表中属性记录数
            int count = attRelationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            if (count > 0) {
                // 修改关联的分组数据
                attRelationService.update(relationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                // 新增关联的分组数据
                relationEntity.setAttrSort(0);
                attRelationService.save(relationEntity);
            }
        }
    }

    /**
     * 通过分组id查询关联的属性信息
     *
     * @param groupId 分组id
     * @return PageUtils
     */
    @Override
    public List<AttrEntity> queryPageAttrgroupRelationAttr(Long groupId) {
        // 获取到分组的关联信息
        List<AttrAttrgroupRelationEntity> relationEntityList = attRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", groupId));
        // 获取到所有的被关联的属性id
        List<Long> attrIds = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        return attrIds.stream().map(item -> baseMapper.selectById(item)).collect(Collectors.toList());
    }

    /**
     * 查询属性分组没有被关联的属性
     * @param params 分页参数
     * @param groupId 属性id
     * @return PageUtils
     */
    @Override
    public PageUtils getAttrgroupNoAttrRelation(Map<String, Object> params, Long groupId) {
        // 根据groupId,查询除分类id
        AttrGroupEntity groupEntity = attrGroupService.getById(groupId);

        // 1.查询出当前分类下的所有基础属性
        List<AttrEntity> attrEntities = baseMapper.selectList(new QueryWrapper<AttrEntity>().select("attr_id").eq("catelog_id", groupEntity.getCatelogId()).eq("attr_type", ProductConstant.attrType.BASE_TYPE.getCode()));
        //   得到所有基础属性id
        List<Long> baseAttrIds = attrEntities.stream().map(AttrEntity::getAttrId).collect(Collectors.toList());

        // 2.查询当前分类下所有已经关联的基础属性
        List<AttrGroupEntity> groupEntityList = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().select("attr_group_id").eq("catelog_id", groupEntity.getCatelogId()));
        //   得到当前分类下所有分组id
        List<Long> groupIds = groupEntityList.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        //   根据分组id在关联表中查询除所有被关联的属性id
        List<AttrAttrgroupRelationEntity> relationEntities = attRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().select("attr_id").in("attr_group_id", groupIds));
        //   得到所有被关联的属性id
        List<Long> linkedAttrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        // 3.过滤哪些已经关联的属性id,得到还没有关联的基础属性
        List<Long> relationAttrIds = baseAttrIds.stream().filter(id -> !linkedAttrIds.contains(id)).collect(Collectors.toList());

        // 4.条件并分页
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().in("attr_id", relationAttrIds);
        // 判断key
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(item -> {
                item.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        return  new PageUtils(page);
    }

    /**
     * 查询spu规则参数
     * @param spuId
     * @return List<ProductAttrValueEntity>
     */
    @Override
    public List<ProductAttrValueEntity> listBaseAttr(Long spuId) {
        return attrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }

    /**
     * 修改spu规格参数
     * @param spuId
     * @param baseAttrVos
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSpuBaseAttr(Long spuId, List<SpuBaseAttrVo> baseAttrVos) {
        // 删除spu原有的规格参数
        attrValueService.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        // 添加新的规格参数
        List<ProductAttrValueEntity> valueEntityList = baseAttrVos.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setSpuId(spuId);
            BeanUtils.copyProperties(item, valueEntity);
            valueEntity.setAttrSort(0);
            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveBatch(valueEntityList);

    }
}