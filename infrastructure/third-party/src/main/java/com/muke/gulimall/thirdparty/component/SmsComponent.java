package com.muke.gulimall.thirdparty.component;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/17 13:39
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SmsComponent {

    private String keyId;

    private String keySecret;

    private String signName;

    private String templateCode;

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return boolean
     */
    public boolean sendCode(String phone, String code) {

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", keyId, keySecret);
        IAcsClient client = new DefaultAcsClient(profile);
        // 构建request对象
        CommonRequest request = new CommonRequest();

        // 短信发送固定配置
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        // 封装code
        Map<String, String> codeMap = new HashMap<>(1);
        codeMap.put("code", code);
        // 短信发送参数设置
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(codeMap));

        try {
            // 发送短信
            CommonResponse response = client.getCommonResponse(request);
            return response.getHttpResponse().isSuccess();
        } catch (ClientException e) {
            log.error("短信验证码发送异常：", e);
            return false;
        }
    }
}
