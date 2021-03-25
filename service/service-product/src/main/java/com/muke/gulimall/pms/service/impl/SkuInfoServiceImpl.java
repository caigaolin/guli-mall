package com.muke.gulimall.pms.service.impl;

import com.muke.gulimall.pms.entity.SkuImagesEntity;
import com.muke.gulimall.pms.entity.SpuInfoDescEntity;
import com.muke.gulimall.pms.service.AttrGroupService;
import com.muke.gulimall.pms.service.SkuImagesService;
import com.muke.gulimall.pms.service.SpuInfoDescService;
import com.muke.gulimall.pms.vo.web.ItemSkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.SkuInfoDao;
import com.muke.gulimall.pms.entity.SkuInfoEntity;
import com.muke.gulimall.pms.service.SkuInfoService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource(name = "skuImagesService")
    private SkuImagesService imagesService;

    @Resource(name = "spuInfoDescService")
    private SpuInfoDescService descService;

    @Resource
    private AttrGroupService attrGroupService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 条件带分页查询
     * @param params 条件及分页参数
     * @return PageUtils
     */
    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)&& !"0".equals(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            BigDecimal bigDecimal = new BigDecimal(min);
            if (bigDecimal.compareTo(new BigDecimal(0)) > 0) {
                queryWrapper.ge("price", bigDecimal);
            }
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            BigDecimal bigDecimal = new BigDecimal(max);
            if (bigDecimal.compareTo(new BigDecimal(0)) > 0) {
                queryWrapper.le("price", bigDecimal);
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 查询商品详情页商品信息
     * @param skuId 商品id
     * @return ItemSkuInfoVo
     */
    @Override
    public ItemSkuInfoVo getItemSkuInfo(Long skuId) throws ExecutionException, InterruptedException {
        ItemSkuInfoVo infoVo = new ItemSkuInfoVo();

        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            // sku基本信息
            SkuInfoEntity skuInfoEntity = baseMapper.selectById(skuId);
            infoVo.setSkuInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = skuInfoFuture.thenAcceptAsync(res -> {
            // spu销售属性组合
            List<ItemSkuInfoVo.SkuItemSaleAttr> itemSaleAttrs = baseMapper.getItemSkuSaleAttr(res.getSpuId());
            infoVo.setItemSaleAttrs(itemSaleAttrs);
        }, executor);

        CompletableFuture<Void> descFuture = skuInfoFuture.thenAcceptAsync(res -> {
            // spu介绍
            SpuInfoDescEntity descEntity = descService.getById(res.getSpuId());
            infoVo.setSpuDesc(descEntity);
        });

        CompletableFuture<Void> baseAttrFuture = skuInfoFuture.thenAcceptAsync(res -> {
            // spu规格参数信息
            List<ItemSkuInfoVo.SpuItemBaseAttr> itemBaseAttrs = attrGroupService.getItemSpuBaseAttr(res.getSpuId(), res.getCatalogId());
            infoVo.setItemBaseAttrs(itemBaseAttrs);
        }, executor);

        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            // sku图片信息
            List<SkuImagesEntity> imagesEntities = imagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_Id", skuId));
            infoVo.setSkuImages(imagesEntities);
        }, executor);

        // 只有当多有异步任务执行完成，才能返回结果
        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imgFuture).get();

        return infoVo;
    }

}