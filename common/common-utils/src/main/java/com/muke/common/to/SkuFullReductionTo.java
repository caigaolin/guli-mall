package com.muke.common.to;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/6 10:41
 */
@Data
public class SkuFullReductionTo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * spu_id
     */
    private Long skuId;
    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
    private Integer addOther;
}
