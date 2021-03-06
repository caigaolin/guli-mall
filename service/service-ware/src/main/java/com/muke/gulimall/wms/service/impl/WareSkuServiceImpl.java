package com.muke.gulimall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.common.constant.WareConstant;
import com.muke.common.enums.OrderStatusEnum;
import com.muke.common.to.SkuStockStatusTo;
import com.muke.common.to.mq.OrderTo;
import com.muke.common.to.mq.WareDetailTaskTo;
import com.muke.common.to.mq.WareLockTo;
import com.muke.common.utils.R;
import com.muke.gulimall.wms.constant.MqConstant;
import com.muke.gulimall.wms.dto.OrderDTO;
import com.muke.gulimall.wms.dto.WareLockDTO;
import com.muke.gulimall.wms.entity.WareOrderTaskDetailEntity;
import com.muke.gulimall.wms.entity.WareOrderTaskEntity;
import com.muke.gulimall.wms.feign.OrderFeign;
import com.muke.gulimall.wms.feign.ProductFeign;
import com.muke.gulimall.wms.service.WareOrderTaskDetailService;
import com.muke.gulimall.wms.service.WareOrderTaskService;
import com.muke.gulimall.wms.vo.SkuStockStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.wms.dao.WareSkuDao;
import com.muke.gulimall.wms.entity.WareSkuEntity;
import com.muke.gulimall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Resource
    private ProductFeign productFeign;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource(name = "wareOrderTaskDetailService")
    private WareOrderTaskDetailService taskDetailService;

    @Resource(name = "wareOrderTaskService")
    private WareOrderTaskService taskService;

    @Resource
    private OrderFeign orderFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ?????????????????????????????????
     * @param params ?????????????????????
     * @return PageUtils
     */
    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        /*skuId:
        wareId*/
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * ????????????
     * @param skuId ??????id
     * @param wareId ??????id
     * @param skuNum ????????????
     */
    @Override
    public void addStore(Long skuId, Long wareId, Integer skuNum) {
        // ??????skuId???wareId????????????
        WareSkuEntity wareSkuEntity = baseMapper.selectOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        // ??????????????????
        if (wareSkuEntity == null) {
            // ??????
            WareSkuEntity saveWareSku = new WareSkuEntity();
            saveWareSku.setSkuId(skuId);
            saveWareSku.setStock(skuNum);
            saveWareSku.setWareId(wareId);
            saveWareSku.setStockLocked(0);
            try {
                R info = productFeign.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    saveWareSku.setSkuName((String) skuInfo.get("skuName"));
                }
            }catch (Exception e) {
                log.error("addStore????????????????????????????????????:{}", e.getMessage());
            }
            baseMapper.insert(saveWareSku);
        } else {
            // ??????
            wareSkuDao.updateStore(wareSkuEntity.getId(), skuNum);
        }
    }

    /**
     * ?????????????????????????????????
     * @param skuIds skuId??????
     * @return List<SkuStockStatusTo>
     */
    @Override
    public List<SkuStockStatusTo> selectSkuStockStatus(List<Long> skuIds) {
        List<SkuStockStatusVo> skuStockStatusVoList = baseMapper.selectSkuStockStatus(skuIds);

        return skuStockStatusVoList.stream().map(item -> {
            SkuStockStatusTo skuStockStatusTo = new SkuStockStatusTo();
            skuStockStatusTo.setSkuId(item.getSkuId());
            skuStockStatusTo.setStockStatus(item.getStock() != null && item.getStock() > 0);
            return skuStockStatusTo;
        }).collect(Collectors.toList());
    }

    /**
     * ????????????
     *      1.?????????sku?????????????????????????????????????????????mq?????????????????????
     *      2.???????????????????????????
     *          1.???????????????????????????????????????????????????30??????????????????????????????????????????
     *          2.??????????????????????????????????????????????????????
     * @param wareLockDTO
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Boolean lockWare(WareLockDTO wareLockDTO) {
        boolean isAllLock = true;
        if (wareLockDTO != null) {
            if (!CollectionUtils.isEmpty(wareLockDTO.getWareInfoList())) {
                // ?????????????????????
                WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
                taskEntity.setOrderSn(wareLockDTO.getOrderSn());
                taskEntity.setCreateTime(new Date());
                taskService.save(taskEntity);
                for (WareLockDTO.WareInfo dto : wareLockDTO.getWareInfoList()) {
                    // ?????????????????????????????????id
                    List<Long> wareIds = baseMapper.selectWareIdByIsStock(dto.getSkuId());
                    if (!CollectionUtils.isEmpty(wareIds)) {
                        boolean isLock = false;
                        for (Long wareId : wareIds) {
                            // ????????????????????????????????????????????????????????????OK
                            Long count = baseMapper.lockWare(wareId, dto.getSkuId(), dto.getNumber());
                            // count???1????????????????????????0????????????
                            if (count.equals(1L)) {
                                // ?????????????????????
                                WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                                detailEntity.setSkuId(dto.getSkuId());
                                detailEntity.setSkuNum(dto.getNumber());
                                detailEntity.setWareId(wareId);
                                detailEntity.setTaskId(taskEntity.getId());
                                detailEntity.setLockStatus(WareConstant.WareLockStatus.LOCKED.getCode());
                                taskDetailService.save(detailEntity);
                                // ????????????
                                WareLockTo lockTo = new WareLockTo();
                                WareDetailTaskTo target = new WareDetailTaskTo();
                                BeanUtils.copyProperties(detailEntity, target);
                                lockTo.setWareDetailTaskTo(target);
                                rabbitTemplate.convertAndSend(MqConstant.WARE_EXCHANGE, MqConstant.DELAY_QUEUE_ROUTING_KEY, lockTo);
                                isLock = true;
                                break;
                            }
                        }
                        // ????????????????????????????????????????????????????????????????????????
                        if (!isLock) {
                            isAllLock = false;
                            break;
                        }
                    } else {
                        isAllLock = false;
                        break;
                    }
                }
            } else {
                isAllLock = false;
            }
        }
        return isAllLock;
    }

    @Override
    public void unLockWareStock(WareLockTo wareLockTo) {
        // ???????????????id
        Long taskId = wareLockTo.getWareDetailTaskTo().getId();
        WareOrderTaskDetailEntity detailEntity = taskDetailService.getById(taskId);
        if (detailEntity != null) {
            // ???????????????
            WareOrderTaskEntity taskEntity = taskService.getById(wareLockTo.getWareDetailTaskTo().getTaskId());
            // ????????????????????????
            R r = orderFeign.getOrderByOrderSn(taskEntity.getOrderSn());
            if (r.getCode().equals(0)) {
                // ????????????????????????
                Object orderObj = r.get("order");
                String orderStr = JSON.toJSONString(orderObj);
                OrderDTO orderDTO = JSON.parseObject(orderStr, OrderDTO.class);
                // ??????????????????
                if (orderDTO == null || orderDTO.getStatus().equals(OrderStatusEnum.CLOSED_ORDER.getCode())) {
                    // ??????????????????????????????????????????????????????
                    if (detailEntity.getLockStatus().equals(WareConstant.WareLockStatus.LOCKED.getCode())) {
                        // ????????????????????????????????????????????????????????????????????????
                        unLockStock(detailEntity.getId(), wareLockTo.getWareDetailTaskTo().getSkuId(), wareLockTo.getWareDetailTaskTo().getWareId(), wareLockTo.getWareDetailTaskTo().getSkuNum());
                    }
                }
            } else {
                throw new RuntimeException("????????????????????????");
            }
        }
    }

    /**
     * ???????????????????????????
     * @param orderTo
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void orderClosedReleaseStock(OrderTo orderTo) {
        WareOrderTaskEntity taskEntity = taskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderTo.getOrderSn()));
        if (taskEntity != null) {
            // ???????????????id????????????????????????????????????????????????????????????????????????
            List<WareOrderTaskDetailEntity> taskDetailEntities = taskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntity.getId()));
            for (WareOrderTaskDetailEntity detailEntity : taskDetailEntities) {
                // ???????????????????????????
                if (WareConstant.WareLockStatus.LOCKED.getCode().equals(detailEntity.getLockStatus())) {
                    // ????????????????????????
                    unLockStock(detailEntity.getId(), detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                }
            }
        }
    }

    /**
     * ????????????
     * @param id
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    private void unLockStock(Long id, Long skuId, Long wareId, Integer skuNum) {
        // ????????????
        wareSkuDao.unLockStock(skuId, wareId, skuNum);
        // ?????????????????????
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(id);
        detailEntity.setLockStatus(WareConstant.WareLockStatus.UN_LOCK.getCode());
        taskDetailService.updateById(detailEntity);
    }

}