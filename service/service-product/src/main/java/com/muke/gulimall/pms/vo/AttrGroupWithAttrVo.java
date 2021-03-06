package com.muke.gulimall.pms.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.muke.gulimall.pms.entity.AttrEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/5 13:49
 */
@Data
public class AttrGroupWithAttrVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 关联属性集合
     */
    private List<AttrEntity> attrs;
}
