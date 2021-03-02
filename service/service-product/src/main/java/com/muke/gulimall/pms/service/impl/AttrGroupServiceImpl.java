package com.muke.gulimall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;
import com.muke.gulimall.pms.dao.AttrGroupDao;
import com.muke.gulimall.pms.entity.AttrGroupEntity;
import com.muke.gulimall.pms.help.CategoryHelp;
import com.muke.gulimall.pms.service.AttrGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private CategoryHelp categoryHelp;

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

}