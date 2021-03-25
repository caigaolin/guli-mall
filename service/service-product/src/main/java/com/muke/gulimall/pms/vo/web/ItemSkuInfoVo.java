package com.muke.gulimall.pms.vo.web;

import com.muke.gulimall.pms.entity.SkuImagesEntity;
import com.muke.gulimall.pms.entity.SkuInfoEntity;
import com.muke.gulimall.pms.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/15 19:23
 */
@ToString
@Data
public class ItemSkuInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * sku基本信息
     */
    private SkuInfoEntity skuInfo;

    /**
     * sku图片集
     */
    private List<SkuImagesEntity> skuImages;

    /**
     * spu商品介绍
     */
    private SpuInfoDescEntity spuDesc;

    /**
     * sku销售属性
     */
    private List<SkuItemSaleAttr> itemSaleAttrs;

    /**
     * spu规则参数
     */
    private List<SpuItemBaseAttr> itemBaseAttrs;

    @ToString
    @Data
    public static class SkuItemSaleAttr implements Serializable{
        private static final long serialVersionUID = 1L;

        private Long attrId;

        private String attrName;

        private List<SkuSaleAttr> saleAttrs;
    }

    @ToString
    @Data
    public static class SkuSaleAttr implements Serializable{
        private static final long serialVersionUID = 1L;

        private String skuIds;

        private String attrValues;
    }

    @ToString
    @Data
    public static class SpuItemBaseAttr implements Serializable{
        private static final long serialVersionUID = 1L;

        private String groupName;

        private List<SpuBaseAttr> baseAttrs;
    }

    @ToString
    @Data
    public static class SpuBaseAttr implements Serializable{
        private static final long serialVersionUID = 1L;

        private String attrName;

        private String attrValue;
    }


}
