package com.muke.gulimall.wms.vo;

import com.muke.common.to.member.MemberReceiveAddressTo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 15:39
 */
@Data
public class MemberReceiveAddressRespVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运费
     */
    private BigDecimal fare;

    private MemberReceiveAddressTo memberAddress;
}
