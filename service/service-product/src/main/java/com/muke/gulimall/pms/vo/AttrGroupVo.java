package com.muke.gulimall.pms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/4 15:08
 */
@Data
public class AttrGroupVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性id
     */
    private Long attrId;

    /**
     * 分组id
     */
    private Long attrGroupId;
}
