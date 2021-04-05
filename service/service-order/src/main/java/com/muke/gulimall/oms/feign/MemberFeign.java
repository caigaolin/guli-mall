package com.muke.gulimall.oms.feign;

import com.muke.gulimall.oms.vo.OrderMemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 16:59
 */
@FeignClient("service-member")
public interface MemberFeign {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<OrderMemberAddressVo> getMemberReceive(@PathVariable("memberId") Long memberId);
}
