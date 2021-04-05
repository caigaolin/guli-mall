package com.muke.gulimall.oms.dto;

import com.muke.common.to.member.MemberReceiveAddressTo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/3 21:49
 */
@Data
public class MemberReceiveAddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运费
     */
    private BigDecimal fare;

    private MemberReceiveAddressTo memberAddress;
}
