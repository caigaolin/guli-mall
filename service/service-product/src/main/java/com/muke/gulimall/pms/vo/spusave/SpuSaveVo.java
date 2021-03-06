/**
  * Copyright 2021 bejson.com 
  */
package com.muke.gulimall.pms.vo.spusave;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2021-03-05 15:11:56
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 商品描述
     */
    private String spuDescription;
    /**
     * 分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 商品重量
     */
    private BigDecimal weight;
    /**
     * 上架状态[0 - 下架，1 - 上架]
     */
    private int publishStatus;

    /**
     * 商品介绍
     */
    private List<String> decript;

    /**
     * 商品图集
     */
    private List<String> images;

    /**
     * 商品积分信息
     */
    private Bounds bounds;

    /**
     * 商品基础属性
     */
    private List<BaseAttrs> baseAttrs;

    /**
     * 商品sku信息
     */
    private List<Skus> skus;

}