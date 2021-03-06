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
     * ?????????????????????????????????
     *
     * @return List<PurchaseEntity>
     */
    @Override
    public List<PurchaseEntity> getUnReceivePurchase() {
        return baseMapper.selectList(new QueryWrapper<PurchaseEntity>().lt("status", WareConstant.PurchaseStatus.RECEIVED.getCode()));
    }

    /**
     * ??????????????????????????????
     *
     * @param mergeVo ????????????
     */
    @Override
    public void mergePurchase(PurchaseMergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null || purchaseId == 0) {
            // ?????????????????????
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
     * ???????????????
     *
     * @param purchaseIds ?????????id??????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void receivedPurchase(List<Long> purchaseIds) {
        List<Long> purIds = purchaseIds.stream().filter(item -> {
            PurchaseEntity purchaseEntity = baseMapper.selectById(item);
            // ?????????????????????????????????????????????
            return purchaseEntity.getStatus().equals(WareConstant.PurchaseStatus.ALLOCATED.getCode());
        }).collect(Collectors.toList());

        // ????????????????????????
        List<PurchaseEntity> purchaseEntities = baseMapper.selectBatchIds(purIds);
        if (!CollectionUtils.isEmpty(purchaseEntities)) {
            List<PurchaseEntity> purchaseEntityList = purchaseEntities.stream().peek(item -> {
                item.setStatus(WareConstant.PurchaseStatus.RECEIVED.getCode());
                item.setUpdateTime(new Date());
            }).collect(Collectors.toList());
            this.updateBatchById(purchaseEntityList);

            // ????????????????????????
            List<PurchaseDetailEntity> detailEntities = detailService.list(new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", purIds));
            List<PurchaseDetailEntity> detailEntityList = detailEntities.stream().peek(item -> {
                item.setStatus(WareConstant.PurchaseDemandStatus.PURCHASING.getCode());
            }).collect(Collectors.toList());
            detailService.updateBatchById(detailEntityList);
        }
    }

    /**
     * ?????????????????????
     *
     * @param doneVo ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void donePurchase(PurchaseDoneVo doneVo) {
        // 1.??????????????????
        List<PurchaseDetailDoneVo> items = doneVo.getItems();
        boolean flag = true;
        List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for (PurchaseDetailDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = detailService.getById(item.getItemId());
            if (purchaseDetailEntity != null && purchaseDetailEntity.getStatus().equals(WareConstant.PurchaseDemandStatus.PURCHASING.getCode())) {
                // ????????????????????????????????????
                if (item.getStatus().equals(WareConstant.PurchaseDemandStatus.HAS_EX.getCode())) {
                    flag = false;
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDemandStatus.HAS_EX.getCode());
                    purchaseDetailEntities.add(purchaseDetailEntity);
                } else {
                    if (item.getStatus().equals(WareConstant.PurchaseDemandStatus.FINISHED.getCode())) {
                        // ???????????????????????????????????????
                        purchaseDetailEntity.setStatus(WareConstant.PurchaseDemandStatus.FINISHED.getCode());
                        // ????????????
                        wareSkuService.addStore(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());
                        purchaseDetailEntities.add(purchaseDetailEntity);
                    }
                }
            }
        }
        // ????????????????????????
        detailService.updateBatchById(purchaseDetailEntities);
        // 2.??????????????????flag???????????????
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        if (flag) {
            // ????????????????????????
            purchaseEntity.setStatus(WareConstant.PurchaseStatus.FINISHED.getCode());
        } else {
            // ????????????????????????
            purchaseEntity.setStatus(WareConstant.PurchaseStatus.HAS_EX.getCode());
        }
        purchaseEntity.setId(doneVo.getId());
        purchaseEntity.setUpdateTime(new Date());
        baseMapper.updateById(purchaseEntity);

    }

}