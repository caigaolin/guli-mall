package com.muke.gulimall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.constant.ProductConstant;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.exception.RRException;
import com.muke.common.to.*;
import com.muke.common.to.es.SpuAttrTo;
import com.muke.common.to.es.SpuUpEsTo;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;
import com.muke.common.utils.R;
import com.muke.gulimall.pms.dao.SpuInfoDao;
import com.muke.gulimall.pms.entity.*;
import com.muke.gulimall.pms.feign.CouponFeign;
import com.muke.gulimall.pms.feign.SearchFeign;
import com.muke.gulimall.pms.feign.WareFeign;
import com.muke.gulimall.pms.service.*;
import com.muke.gulimall.pms.vo.spusave.Images;
import com.muke.gulimall.pms.vo.spusave.SpuSaveVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource(name = "productAttrValueService")
    private ProductAttrValueService attrValueService;

    @Resource
    private CouponFeign couponFeign;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource(name = "skuSaleAttrValueService")
    private SkuSaleAttrValueService saleAttrValueService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private BrandService brandService;

    @Resource
    private WareFeign wareFeign;

    @Resource
    private AttrService attrService;

    @Resource
    private SearchFeign searchFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ????????????
     *
     * @param spuInfo spu??????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuGoods(SpuSaveVo spuInfo) {
        // 1.????????????spu???????????? pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        baseMapper.insert(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();
        // 2.????????????spu???????????? pms_spu_info_desc
        SpuInfoDescEntity infoDescEntity = new SpuInfoDescEntity();
        infoDescEntity.setSpuId(spuId);
        infoDescEntity.setDecript(String.join(",", spuInfo.getDecript()));
        spuInfoDescService.save(infoDescEntity);
        // 3.????????????spu???????????? pms_spu_images
        List<SpuImagesEntity> imagesEntityList = spuInfo.getImages().stream().map(image -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuId);
            spuImagesEntity.setImgName("");
            spuImagesEntity.setImgSort(0);
            spuImagesEntity.setDefaultImg(0);
            spuImagesEntity.setImgUrl(image);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(imagesEntityList);
        // 4.????????????spu???????????? pms_product_attr_value
        List<ProductAttrValueEntity> attrValueEntityList = spuInfo.getBaseAttrs().stream().map(baseAttrs -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setSpuId(spuId);
            valueEntity.setAttrId(baseAttrs.getAttrId());
            valueEntity.setAttrName(baseAttrs.getAttrName());
            valueEntity.setAttrValue(baseAttrs.getAttrValues());
            valueEntity.setQuickShow(baseAttrs.getShowDesc());
            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveBatch(attrValueEntityList);
        // 5.??????????????????spu?????????????????? gulimall_sms ==??? sms_spu_bounds
        // ????????????????????????????????????
        if (spuInfo.getBounds().getBuyBounds().compareTo(new BigDecimal(0)) > 0 ||
                spuInfo.getBounds().getGrowBounds().compareTo(new BigDecimal(0)) > 0) {
            SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
            spuBoundsTo.setSpuId(spuId);
            spuBoundsTo.setBuyBounds(spuInfo.getBounds().getBuyBounds());
            spuBoundsTo.setGrowBounds(spuInfo.getBounds().getGrowBounds());
            R r = couponFeign.saveSpuBounds(spuBoundsTo);
            if (r.getCode() != 0) {
                throw new RRException(CustomizeExceptionEnum.SAVE_BOUNDS_FAIL);
            }
        }

        // 6.????????????sku??????
        spuInfo.getSkus().forEach(sku -> {
            // ????????????????????????
            List<Images> images = sku.getImages();
            String defaultImg = "";
            for (Images image : images) {
                if (image.getDefaultImg() == 1 && !StringUtils.isEmpty(image.getImgUrl())) {
                    defaultImg = image.getImgUrl();
                }
            }
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setCatalogId(spuInfo.getCatalogId());
            skuInfoEntity.setBrandId(spuInfo.getBrandId());
            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setSkuDefaultImg(defaultImg);
            skuInfoEntity.setSkuDesc("");
            BeanUtils.copyProperties(sku, skuInfoEntity);
            //   6.1.??????sku???????????? pms_sku_info
            skuInfoService.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            List<SkuImagesEntity> skuImagesEntityList = images.stream()
                    .filter(image -> !StringUtils.isEmpty(image.getImgUrl()))
                    .map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setDefaultImg(image.getDefaultImg());
                        skuImagesEntity.setImgUrl(image.getImgUrl());
                        skuImagesEntity.setImgSort(0);
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
            //   6.2.??????sku???????????? pms_sku_images
            skuImagesService.saveBatch(skuImagesEntityList);

            List<SkuSaleAttrValueEntity> attrValueEntities = sku.getAttr().stream().map(attr -> {
                SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                attrValueEntity.setSkuId(skuId);
                attrValueEntity.setAttrSort(0);
                BeanUtils.copyProperties(attr, attrValueEntity);
                return attrValueEntity;
            }).collect(Collectors.toList());
            //   6.3.??????sku???????????? pms_sku_sale_attr_value
            saleAttrValueService.saveBatch(attrValueEntities);

            //   6.4.????????????sku?????????????????????????????? gulimall_sms ==???sms_sku_ladder sms_sku_full_reduction
            if (sku.getFullCount() != 0 || sku.getDiscount().compareTo(new BigDecimal(0)) > 0) {
                SkuLadderTo skuLadderTo = new SkuLadderTo();
                skuLadderTo.setSkuId(skuId);
                skuLadderTo.setFullCount(sku.getFullCount());
                skuLadderTo.setDiscount(sku.getDiscount());
                skuLadderTo.setPrice(sku.getPrice().multiply(sku.getDiscount()));
                skuLadderTo.setAddOther(sku.getCountStatus());
                R rLadder = couponFeign.saveSkuLadder(skuLadderTo);
                if (rLadder.getCode() != 0) {
                    throw new RRException(CustomizeExceptionEnum.SAVE_LADDER_FAIL);
                }
            }

            if (sku.getFullPrice().compareTo(new BigDecimal(0)) > 0 || sku.getReducePrice().compareTo(new BigDecimal(0)) > 0) {
                SkuFullReductionTo skuFullReductionTo = new SkuFullReductionTo();
                skuFullReductionTo.setSkuId(skuId);
                skuFullReductionTo.setFullPrice(sku.getFullPrice());
                skuFullReductionTo.setReducePrice(sku.getReducePrice());
                skuFullReductionTo.setAddOther(sku.getPriceStatus());
                R rReduction = couponFeign.saveSkuFullReduction(skuFullReductionTo);
                if (rReduction.getCode() != 0) {
                    throw new RRException(CustomizeExceptionEnum.SAVE_FULL_REDUCTION_FAIL);
                }
            }

            //   6.5.?????????????????????????????? gulimall_sms ==??? sms_member_price
            List<SkuMemberPriceTo> skuMemberPriceToList = sku.getMemberPrice().stream()
                    .filter(memberPrice -> memberPrice.getPrice().compareTo(new BigDecimal(0)) > 0)
                    .map(memberPrice -> {
                        SkuMemberPriceTo skuMemberPriceTo = new SkuMemberPriceTo();
                        skuMemberPriceTo.setSkuId(skuId);
                        skuMemberPriceTo.setMemberLevelId(memberPrice.getId());
                        skuMemberPriceTo.setMemberLevelName(memberPrice.getName());
                        skuMemberPriceTo.setMemberPrice(memberPrice.getPrice());
                        skuMemberPriceTo.setAddOther(0);
                        return skuMemberPriceTo;
                    }).collect(Collectors.toList());
            R rMember = couponFeign.saveMemberPrice(skuMemberPriceToList);
            if (rMember.getCode() != 0) {
                throw new RRException(CustomizeExceptionEnum.SAVE_MEMBER_PRICE_FAIL);
            }
        });

    }

    /**
     * ?????????????????????spu??????
     * @param params ?????????????????????
     * @return PageUtils
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(obj -> {
                obj.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)&& !"0".equals(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * ????????????
     * @param spuId
     */
    @Override
    public void spuUp(Long spuId) {
        // ???????????????????????????
        // 1.??????spuId?????????????????????sku????????????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        // ???????????????sku???id??????
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        // hasStock??????????????????????????????????????????sku??????????????????
        List<SkuStockStatusTo> skuStockStatusToList = wareFeign.getSkuStockStatus(skuIds);

        // ???????????????????????????????????????
        List<AttrEntity> attrEntities = attrService.list(new QueryWrapper<AttrEntity>().select("attr_id").eq("search_type", ProductConstant.attrSearchStatus.CAN_INDEX.getCode()).eq("attr_type", ProductConstant.attrType.BASE_TYPE.getCode()));
        List<Long> allAttrIds = attrEntities.stream().map(AttrEntity::getAttrId).collect(Collectors.toList());
        // ???????????????spu?????????????????????
        List<ProductAttrValueEntity> attrValueEntityList = attrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        // ???????????????????????????????????????????????????????????????SpuAttrTo?????????
        List<SpuAttrTo> attrToList = attrValueEntityList.stream().filter(attrValue -> allAttrIds.contains(attrValue.getAttrId())).map(attrValue -> {
            SpuAttrTo spuAttrTo = new SpuAttrTo();
            BeanUtils.copyProperties(attrValue, spuAttrTo);
            return spuAttrTo;
        }).collect(Collectors.toList());

        // ?????????????????????es???????????????
        List<SpuUpEsTo> spuUpEsToList = skuInfoEntities.stream().map(sku -> {
            SpuUpEsTo spuUpEsTo = new SpuUpEsTo();
            BeanUtils.copyProperties(sku, spuUpEsTo);
            spuUpEsTo.setSkuPrice(sku.getPrice());
            spuUpEsTo.setSkuImg(sku.getSkuDefaultImg());
            spuUpEsTo.setCatelogId(sku.getCatalogId());
            // ???????????????????????????0
            spuUpEsTo.setHotScore(0L);
            // catelogName???????????????
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            spuUpEsTo.setCatelogName(categoryEntity.getName());
            // brandName,brandImg???????????????
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            spuUpEsTo.setBrandImg(brandEntity.getLogo());
            spuUpEsTo.setBrandName(brandEntity.getName());

            // ??????????????????
            for (SkuStockStatusTo skuStockStatusTo : skuStockStatusToList) {
                if (sku.getSkuId().equals(skuStockStatusTo.getSkuId())) {
                    spuUpEsTo.setHasStock(skuStockStatusTo.getStockStatus());
                }
            }

            // ??????attrs
            spuUpEsTo.setAttrs(attrToList);
            return spuUpEsTo;
        }).collect(Collectors.toList());

        // 2.????????????search??????????????????????????????es???
        R r = searchFeign.saveSpuToEs(spuUpEsToList);
        if (r.getCode() == 0) {
            // ????????????
            log.info("SpuInfoServiceImpl==???spuUp??????????????????????????????ES??????");
            // 3.??????spu????????????
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setPublishStatus(ProductConstant.productPublishStatus.UP.getCode());
            spuInfoEntity.setUpdateTime(new Date());
            baseMapper.updateById(spuInfoEntity);
        } else {
            // ????????????
            log.info("SpuInfoServiceImpl==???spuUp??????????????????????????????ES??????");
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        return baseMapper.selectById(spuId);
    }

}