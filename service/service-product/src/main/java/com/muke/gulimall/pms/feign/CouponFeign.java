package com.muke.gulimall.pms.feign;

import com.muke.common.to.SkuFullReductionTo;
import com.muke.common.to.SkuLadderTo;
import com.muke.common.to.SkuMemberPriceTo;
import com.muke.common.to.SpuBoundsTo;
import com.muke.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/5 20:45
 */
@FeignClient("service-coupon")
public interface CouponFeign {

    /**
     * 保存spu优惠价格信息
     * @param spuBoundsTo 优惠实体
     * @return R
     */
    @PostMapping("/sms/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    /**
     * 保存sku阶梯价格信息
     * @param skuLadderTo 阶梯价格实体
     * @return R
     */
    @PostMapping("/sms/skuladder/save")
    R saveSkuLadder(@RequestBody SkuLadderTo skuLadderTo);

    /**
     * 保存sku满减信息
     * @param skuFullReductionTo 满减信息实体
     * @return R
     */
    @PostMapping("/sms/skufullreduction/save")
    R saveSkuFullReduction(@RequestBody SkuFullReductionTo skuFullReductionTo);

    /**
     * 保存会员价格信息
     * @param skuMemberPriceToList 会员价信息集合
     * @return R
     */
    @PostMapping("/sms/memberprice/save/batch")
    R saveMemberPrice(@RequestBody List<SkuMemberPriceTo> skuMemberPriceToList);
}
