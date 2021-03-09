package com.muke.gulimall.pms.feign;

import com.muke.common.to.SkuStockStatusTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 15:08
 */
@FeignClient("service-ware")
public interface WareFeign {

    /**
     * 检查商品是否存在库存
     * @param skuIds skuId集合
     * @return List<SkuStockStatusTo>
     */
    @PostMapping("/ware/waresku/stock/status")
    List<SkuStockStatusTo> getSkuStockStatus(List<Long> skuIds);
}
