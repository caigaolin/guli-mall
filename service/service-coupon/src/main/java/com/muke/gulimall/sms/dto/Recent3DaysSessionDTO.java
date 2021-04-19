package com.muke.gulimall.sms.dto;

import com.muke.gulimall.sms.entity.SeckillSessionEntity;
import com.muke.gulimall.sms.entity.SeckillSkuRelationEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/15 20:02
 */
@Data
public class Recent3DaysSessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private SeckillSessionEntity sessionEntity;

    private List<SeckillSkuRelationEntity> relationEntities;

}
