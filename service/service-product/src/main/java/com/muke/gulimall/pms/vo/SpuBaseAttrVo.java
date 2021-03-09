package com.muke.gulimall.pms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/7 13:36
 */
@Data
public class SpuBaseAttrVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long attrId;

    private String attrName;

    private String attrValue;

    private Integer quickShow;
}
