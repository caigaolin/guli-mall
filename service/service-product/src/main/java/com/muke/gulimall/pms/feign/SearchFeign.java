package com.muke.gulimall.pms.feign;

import com.muke.common.to.es.SpuUpEsTo;
import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 16:26
 */
@FeignClient("service-search")
public interface SearchFeign {

    /**
     * 保存商品上架数据至ES
     * @param spuUpEsToList 商品上架实体集合
     * @return R
     */
    @PostMapping("es/save/spu")
    R saveSpuToEs(@RequestBody List<SpuUpEsTo> spuUpEsToList);
}
