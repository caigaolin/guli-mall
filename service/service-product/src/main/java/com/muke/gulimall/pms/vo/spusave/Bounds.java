/**
  * Copyright 2021 bejson.com 
  */
package com.muke.gulimall.pms.vo.spusave;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Auto-generated: 2021-03-05 15:11:56
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Bounds implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 购买积分
     */
    private BigDecimal buyBounds;

    /**
     * 成长值
     */
    private BigDecimal growBounds;

}