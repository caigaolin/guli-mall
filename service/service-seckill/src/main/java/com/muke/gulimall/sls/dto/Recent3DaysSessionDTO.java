package com.muke.gulimall.sls.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/15 20:41
 */
@Data
public class Recent3DaysSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private SeckillSessionDto sessionEntity;

    private List<SeckillSkuRelationDto> relationEntities;

}
