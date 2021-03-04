package com.muke.gulimall.pms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/4 10:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AttrRespVo extends AttrVo {

    /**
     * 分类名
     */
    private String catelogName;

    /**
     * 分组名
     */
    private String groupName;

    /**
     * 分类所属的完整id
     */
    private Long[] catelogPath;

    /**
     * 属性分组id
     */
    private Long attrGroupId;
}
