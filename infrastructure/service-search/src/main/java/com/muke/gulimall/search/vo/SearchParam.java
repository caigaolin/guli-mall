package com.muke.gulimall.search.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/12 20:06
 */
@Data
public class SearchParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 全文检索字段
     */
    private String keyword;

    /**
     * 分类3级id
     */
    private Long catalog3Id;

    /**
     * 排序：
     *     sort=saleCount_desc/asc
     *     sort=skuPrice_desc/asc
     *     sort=hotScore_desc/asc
     */
    private String sort;

    /**
     * 是否有货 默认有货
     *       hasStock=0 / 1
     */
    private Integer hasStock;

    /**
     * 价格区间
     *      skuPrice=1_500 / _500 / 500_
     */
    private String skuPrice;

    /**
     * 品牌id
     */
    private List<Long> brandId;

    /**
     * 规格属性
     *       attrs=1_3G:4G:5G
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;
}
