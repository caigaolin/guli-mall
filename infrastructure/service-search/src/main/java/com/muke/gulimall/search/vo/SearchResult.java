package com.muke.gulimall.search.vo;

import com.muke.common.to.es.SpuUpEsTo;
import lombok.Data;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/12 20:08
 */
@Data
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * es中保存的商品数据
     */
    private List<SpuUpEsTo> products;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 页码项
     */
    private List<Integer> pageNav;

    /**
     * 当前查询到的结果，所涉及的品牌
     */
    private List<BrandVo> brands;
    /**
     * 当前查询到的结果，所涉及的分类
     */
    private List<CategoryVo> catalogs;
    /**
     * 当前查询到的结果，所涉及的属性
     */
    private List<AttrVo> attrs;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String branImg;
    }

    @Data
    public static class CategoryVo {
        private Long catelogId;
        private String catelogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }


}
