package com.muke.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muke.common.utils.PageUtils;
import com.muke.gulimall.pms.entity.SpuInfoEntity;
import com.muke.gulimall.pms.vo.spusave.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author muke
 * @email mark-loy@163.com
 * @date 2021-02-26 10:43:54
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuGoods(SpuSaveVo spuInfo);

    PageUtils queryPageByCondition(Map<String, Object> params);
}

