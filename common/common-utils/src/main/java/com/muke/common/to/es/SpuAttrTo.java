package com.muke.common.to.es;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 14:31
 */
@Data
public class SpuAttrTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long attrId;

    private String attrName;

    private String attrValue;
}
