package com.muke.gulimall.auth.feign;

import com.muke.common.utils.R;
import com.muke.gulimall.auth.vo.LoginVo;
import com.muke.gulimall.auth.vo.RegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/17 19:06
 */
@FeignClient("service-member")
public interface MemberFeign {

    @PostMapping("/member/member/register")
    R registerMember(@RequestBody RegisterVo registerVo);

    @PostMapping("/member/member/login")
    R loginMember(@RequestBody LoginVo loginVo);
}
