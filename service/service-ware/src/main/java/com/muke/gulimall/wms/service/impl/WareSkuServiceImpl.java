package com.muke.gulimall.wms.service.impl;

import com.muke.common.to.SkuStockStatusTo;
import com.muke.common.utils.R;
import com.muke.gulimall.wms.feign.ProductFeign;
import com.muke.gulimall.wms.vo.SkuStockStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Resource
    private ProductFeign productFeign;

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

}