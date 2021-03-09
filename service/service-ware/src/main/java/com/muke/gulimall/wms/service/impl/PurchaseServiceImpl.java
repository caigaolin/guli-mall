package com.muke.gulimall.wms.service.impl;

import com.muke.common.constant.WareConstant;
import com.muke.gulimall.wms.entity.PurchaseDetailEntity;
import com.muke.gulimall.wms.service.PurchaseDetailService;
import com.muke.gulimall.wms.service.WareSkuService;
import com.muke.gulimall.wms.vo.PurchaseDetailDoneVo;
import com.muke.gulimall.wms.vo.PurchaseDoneVo;
import com.muke.gulimall.wms.vo.PurchaseMergeVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.wms.dao.PurchaseDao;
import com.muke.gulimall.wms.entity.PurchaseEntity;
import com.muke.gulimall.wms.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource(name = "purchaseDetailService")
    private PurchaseDetailService detailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("assignee_id", key).or().like("assignee_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 查询未领取的采购单信息
     *
     * @return List<PurchaseEntity>
     */
    @Override
    public List<PurchaseEntity> getUnReceivePurchase() {
        return baseMapper.selectList(new QueryWrapper<PurchaseEntity>().lt("status", WareConstant.PurchaseStatus.RECEIVED.getCode()));
    }

    /**
     * 合并采购需求到采购单
     *
     * @param mergeVo 合并实体
     */
    @Override
    public void mergePurchase(PurchaseMergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null || purchaseId == 0) {
            // 需要新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(0);
            purchaseEntity.setPriority(1);
            baseMapper.insert(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> detailEntityList = mergeVo.getItems().stream().filter(item -> {
            PurchaseDetailEntity detailEntity = detailService.getById(item);
            Integer status = detailEntity.getStatus();
            return status < WareConstant.PurchaseDemandStatus.PURCHASING.getCode();
        }).map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(1);
            return detailEntity;
        }).collect(Collectors.toList());
        detailService.updateBatchById(detailEntityList);
    }

    /**
     * 领取采购单
     *
     * @param purchaseIds 采购单id集合
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void receivedPurchase(List<Long> purchaseIds) {
        List<Long> purIds = purchaseIds.stream().filter(item -> {
            PurchaseEntity purchaseEntity = baseMapper.selectById(item);
            // 过滤哪些状态不是已分配的采购单
            return purchaseEntity.getStatus().equals(WareConstant.PurchaseStatus.ALLOCATED.getCode());
        }).collect(Collectors.toList());

        // 查询除所有采购单
        List<PurchaseEntity> purchaseEntities = baseMapper.selectBatchIds(purIds);
        if (!CollectionUtils.isEmpty(purchaseEntities)) {
            List<PurchaseEntity> purchaseEntityList = purchaseEntities.stream().peek(item -> {
                item.setStatus(WareConstant.PurchaseStatus.RECEIVED.getCode());
                item.setUpdateTime(new Date());
            }).collect(Collectors.toList());
            this.updateBatchById(purchaseEntityList);

            // 查询所有采购需求
            List<PurchaseDetailEntity> detailEntities = detailService.list(new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", purIds));
            List<PurchaseDetailEntity> detailEntityList = detailEntities.stream().peek(item -> {
                item.setStatus(WareConstant.PurchaseDemandStatus.PURCHASING.getCode());
            }).collect(Collectors.toList());
            detailService.updateBatchById(detailEntityList);
        }
    }

    /**
     * 完成采购及入库
     *
     * @param doneVo 完成实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void donePurchase(PurchaseDoneVo doneVo) {
        // 1.修改采购需求
        List<PurchaseDetailDoneVo> items = doneVo.getItems();
        boolean flag = true;
        List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for (PurchaseDetailDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = detailService.getById(item.getItemId());
            if (purchaseDetailEntity != null && purchaseDetailEntity.getStatus().equals(WareConstant.PurchaseDemandStatus.PURCHASING.getCode())) {
                // 判断采购需求状态是否正常
                if (item.getStatus().equals(WareConstant.PurchaseDemandStatus.HAS_EX.getCode())) {
                    flag = false;
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDemandStatus.HAS_EX.getCode());
                    purchaseDetailEntities.add(purchaseDetailEntity);
                } else {
                    if (item.getStatus().equals(WareConstant.PurchaseDemandStatus.FINISHED.getCode())) {
                        // 只有正在采购的需求才能入库
                        purchaseDetailEntity.setStatus(WareConstant.PurchaseDemandStatus.FINISHED.getCode());
                        // 商品入库
                        wareSkuService.addStore(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());
                        purchaseDetailEntities.add(purchaseDetailEntity);
                    }
                }
            }
        }
        // 批量更新采购需求
        detailService.updateBatchById(purchaseDetailEntities);
        // 2.根据采购需求flag修改采购单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        if (flag) {
            // 采购需求全部完成
            purchaseEntity.setStatus(WareConstant.PurchaseStatus.FINISHED.getCode());
        } else {
            // 采购需求存在异常
            purchaseEntity.setStatus(WareConstant.PurchaseStatus.HAS_EX.getCode());
        }
        purchaseEntity.setId(doneVo.getId());
        purchaseEntity.setUpdateTime(new Date());
        baseMapper.updateById(purchaseEntity);

    }

}