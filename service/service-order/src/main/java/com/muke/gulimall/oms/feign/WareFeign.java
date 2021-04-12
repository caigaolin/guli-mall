package com.muke.gulimall.oms.feign;

import com.muke.common.to.SkuStockStatusTo;
import com.muke.common.utils.R;
import com.muke.gulimall.oms.dto.WareLockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/31 21:39
 */
@FeignClient("service-ware")
public interface WareFeign {

    @PostMapping("/ware/waresku/stock/status")
    List<SkuStockStatusTo> getSkuStockStatus(@RequestBody List<Long> skuIds);

    @PostMapping("/ware/waresku/lock")
    R lockWare(@RequestBody WareLockDTO wareLockDTOS);

    @GetMapping("/ware/wareinfo/fare/{addrId}")
    R getFare(@PathVariable("addrId") Long addrId);
}
