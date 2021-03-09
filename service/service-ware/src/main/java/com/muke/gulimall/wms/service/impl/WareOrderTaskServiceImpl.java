package com.muke.gulimall.wms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.wms.dao.WareOrderTaskDao;
import com.muke.gulimall.wms.entity.WareOrderTaskEntity;
import com.muke.gulimall.wms.service.WareOrderTaskService;
import org.springframework.util.StringUtils;


@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity> implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareOrderTaskEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
               wrapper.eq("id", key).or().like("order_id", key).like("consignee", key);
            });
        }
        IPage<WareOrderTaskEntity> page = this.page(
                new Query<WareOrderTaskEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

}