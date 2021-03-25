package com.muke.gulimall.cart.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/23 21:31
 */
@Data
public class MemberInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userKey;

    private Boolean isTemUser;
}
