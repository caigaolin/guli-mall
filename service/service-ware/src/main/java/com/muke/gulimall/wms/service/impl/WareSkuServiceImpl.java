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
     * 条件带分页查询库存商品
     * @param params 条件及分页参数
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
     * 添加库存
     * @param skuId 商品id
     * @param wareId 仓库id
     * @param skuNum 商品数量
     */
    @Override
    public void addStore(Long skuId, Long wareId, Integer skuNum) {
        // 根据skuId、wareId查询库存
        WareSkuEntity wareSkuEntity = baseMapper.selectOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        // 判断商品库存
        if (wareSkuEntity == null) {
            // 新增
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
                log.error("addStore远程调用商品服务出现异常:{}", e.getMessage());
            }
            baseMapper.insert(saveWareSku);
        } else {
            // 修改
            wareSkuDao.updateStore(wareSkuEntity.getId(), skuNum);
        }
    }

    /**
     * 检查并返回库存状态结果
     * @param skuIds skuId集合
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
     * 锁定库存
     *      1.只要有sku锁定库存成功，就发送一个消息到mq，进入延迟队列
     *      2.解锁库存有如下情况
     *          1.用户下订单成功，但是没有进行支付，30分钟后自动取消订单，解锁库存
     *          2.用户下单成功，主动取消订单，解锁库存
     * @param wareLockDTO
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Boolean lockWare(WareLockDTO wareLockDTO) {
        boolean isAllLock = true;
        if (wareLockDTO != null) {
            if (!CollectionUtils.isEmpty(wareLockDTO.getWareInfoList())) {
                // 保存库存工作单
                WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
                taskEntity.setOrderSn(wareLockDTO.getOrderSn());
                taskEntity.setCreateTime(new Date());
                taskService.save(taskEntity);
                for (WareLockDTO.WareInfo dto : wareLockDTO.getWareInfoList()) {
                    // 获取到所有有库存的仓库id
                    List<Long> wareIds = baseMapper.selectWareIdByIsStock(dto.getSkuId());
                    if (!CollectionUtils.isEmpty(wareIds)) {
                        boolean isLock = false;
                        for (Long wareId : wareIds) {
                            // 进行挨个仓库锁库存，只要有一个锁成功，就OK
                            Long count = baseMapper.lockWare(wareId, dto.getSkuId(), dto.getNumber());
                            // count为1表示锁库存成功，0表示失败
                            if (count.equals(1L)) {
                                // 保存工作单详情
                                WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                                detailEntity.setSkuId(dto.getSkuId());
                                detailEntity.setSkuNum(dto.getNumber());
                                detailEntity.setWareId(wareId);
                                detailEntity.setTaskId(taskEntity.getId());
                                detailEntity.setLockStatus(WareConstant.WareLockStatus.LOCKED.getCode());
                                taskDetailService.save(detailEntity);
                                // 发送消息
                                WareLockTo lockTo = new WareLockTo();
                                WareDetailTaskTo target = new WareDetailTaskTo();
                                BeanUtils.copyProperties(detailEntity, target);
                                lockTo.setWareDetailTaskTo(target);
                                rabbitTemplate.convertAndSend(MqConstant.WARE_EXCHANGE, MqConstant.DELAY_QUEUE_ROUTING_KEY, lockTo);
                                isLock = true;
                                break;
                            }
                        }
                        // 判断当前商品是否锁成功，失败则跳出整个锁库存操作
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
        // 工作单详情id
        Long taskId = wareLockTo.getWareDetailTaskTo().getId();
        WareOrderTaskDetailEntity detailEntity = taskDetailService.getById(taskId);
        if (detailEntity != null) {
            // 查询工作单
            WareOrderTaskEntity taskEntity = taskService.getById(wareLockTo.getWareDetailTaskTo().getTaskId());
            // 远程查询订单状态
            R r = orderFeign.getOrderByOrderSn(taskEntity.getOrderSn());
            if (r.getCode().equals(0)) {
                // 订单服务调用成功
                Object orderObj = r.get("order");
                String orderStr = JSON.toJSONString(orderObj);
                OrderDTO orderDTO = JSON.parseObject(orderStr, OrderDTO.class);
                // 判断订单状态
                if (orderDTO == null || orderDTO.getStatus().equals(OrderStatusEnum.CLOSED_ORDER.getCode())) {
                    // 只有订单状态为已关闭才能进行解锁库存
                    if (detailEntity.getLockStatus().equals(WareConstant.WareLockStatus.LOCKED.getCode())) {
                        // 只有当前工作详情单状态为已锁定，才能进行解锁库存
                        unLockStock(detailEntity.getId(), wareLockTo.getWareDetailTaskTo().getSkuId(), wareLockTo.getWareDetailTaskTo().getWareId(), wareLockTo.getWareDetailTaskTo().getSkuNum());
                    }
                }
            } else {
                throw new RuntimeException("订单服务调用失败");
            }
        }
    }

    /**
     * 订单关闭，解锁库存
     * @param orderTo
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void orderClosedReleaseStock(OrderTo orderTo) {
        WareOrderTaskEntity taskEntity = taskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderTo.getOrderSn()));
        if (taskEntity != null) {
            // 拿着工作单id去工作单详情表中查询，工作详情的状态必须为已锁定
            List<WareOrderTaskDetailEntity> taskDetailEntities = taskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntity.getId()));
            for (WareOrderTaskDetailEntity detailEntity : taskDetailEntities) {
                // 判断工作单详情状态
                if (WareConstant.WareLockStatus.LOCKED.getCode().equals(detailEntity.getLockStatus())) {
                    // 已锁定则需要解锁
                    unLockStock(detailEntity.getId(), detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                }
            }
        }
    }

    /**
     * 解锁库存
     * @param id
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    private void unLockStock(Long id, Long skuId, Long wareId, Integer skuNum) {
        // 解锁库存
        wareSkuDao.unLockStock(skuId, wareId, skuNum);
        // 更新工作单状态
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(id);
        detailEntity.setLockStatus(WareConstant.WareLockStatus.UN_LOCK.getCode());
        taskDetailService.updateById(detailEntity);
    }

}