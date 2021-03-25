package com.muke.gulimall.thirdparty.controller;

import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.utils.R;
import com.muke.gulimall.thirdparty.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/17 11:30
 */
@RestController
public class SmsController {

    @Resource
    private SmsComponent smsComponent;

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return R
     */
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        boolean isSuccess = smsComponent.sendCode(phone, code);
        if (isSuccess) {
            return R.ok();
        } else {
            return R.error(CustomizeExceptionEnum.SEND_CODE_EX);
        }

    }

}
