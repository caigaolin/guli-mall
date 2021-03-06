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
public class Skus implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品销售属性
     */
    private List<Attr> attr;
    /**
     * 销售商品名称
     */
    private String skuName;
    /**
     * 销售价格
     */
    private BigDecimal price;
    /**
     * 标题
     */
    private String skuTitle;
    /**
     * 副标题
     */
    private String skuSubtitle;
    /**
     * 商品图集
     */
    private List<Images> images;
    /**
     * 笛卡尔销售属性
     */
    private List<String> descar;

    /**
     * 满减数量
     */
    private int fullCount;
    /**
     * 满减折扣
     */
    private BigDecimal discount;
    /**
     * 是否可数量满减叠加 [1:可叠加；0:不可叠加]
     */
    private int countStatus;

    /**
     * 满减价格
     */
    private BigDecimal fullPrice;
    /**
     * 优惠价格
     */
    private BigDecimal reducePrice;
    /**
     * 是否可价格满减叠加 [1:可叠加；0:不可叠加]
     */
    private int priceStatus;
    /**
     * 会员价格信息
     */
    private List<MemberPrice> memberPrice;


}