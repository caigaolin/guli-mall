package com.muke.gulimall.pms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/5 12:40
 */
@Data
public class BrandRepsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;
}
