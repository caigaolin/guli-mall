package com.muke.gulimall.sms.service.impl;

import com.muke.gulimall.sms.dto.Recent3DaysSessionDTO;
import com.muke.gulimall.sms.entity.SeckillSkuRelationEntity;
import com.muke.gulimall.sms.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.sms.dao.SeckillSessionDao;
import com.muke.gulimall.sms.entity.SeckillSessionEntity;
import com.muke.gulimall.sms.service.SeckillSessionService;

import javax.annotation.Resource;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource(name = "seckillSkuRelationService")
    private SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取最近3天的上架活动信息
     * @return
     */
    @Override
    public List<Recent3DaysSessionDTO> getRecent3DaysSession() {
        // 获取最近3天的活动信息
        List<SeckillSessionEntity> sessionEntityList = baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", getStartDateTime(), getEndDateTime()));
        return sessionEntityList.stream().map(seckillSessionEntity -> {
            // 获取需要上架的商品关联信息
            List<SeckillSkuRelationEntity> relationEntities = relationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                    .eq("promotion_session_id", seckillSessionEntity.getId()));
            Recent3DaysSessionDTO daysSessionDTO = new Recent3DaysSessionDTO();
            daysSessionDTO.setSessionEntity(seckillSessionEntity);
            daysSessionDTO.setRelationEntities(relationEntities);
            return daysSessionDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取开始日期时间
     * @return
     */
    private String getStartDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取结束日期时间
     * @return
     */
    private String getEndDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}