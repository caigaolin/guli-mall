package com.muke.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.muke.common.constant.AuthConstant;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.utils.R;
import com.muke.common.vo.MemberRespVo;
import com.muke.gulimall.auth.feign.MemberFeign;
import com.muke.gulimall.auth.feign.SmsFeign;
import com.muke.gulimall.auth.utils.RandomUtil;
import com.muke.gulimall.auth.vo.LoginVo;
import com.muke.gulimall.auth.vo.RegisterVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/16 19:44
 */
@Controller
public class AuthController {

    @Resource
    private SmsFeign smsFeign;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redis;

    @Resource
    private MemberFeign memberFeign;

    /**
     * 获取短信验证码
     * @return String
     */
    @ResponseBody
    @GetMapping("/sms/send/{phone}/code")
    public R getPhoneCode(@PathVariable("phone") String phone) {
        // 从redis中获取验证码
        String redisCode = redis.opsForValue().get(AuthConstant.CODE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            // 获取保存验证码的时间
            long saveTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - saveTime <= 60000) {
                return R.error(CustomizeExceptionEnum.SEND_CODE_OFTEN);
            }
        }
        // 生成随机验证码
        String code = RandomUtil.getSixBitRandom();
        // 远程调用发送验证码
        R r = smsFeign.sendCode(phone, code);
        if (!r.getCode().equals(0)) {
            return R.error(CustomizeExceptionEnum.SEND_CODE_EX);
        }
        // 将验证码存入redis,有效期为十分钟
        redis.opsForValue().set(AuthConstant.CODE_PREFIX + phone, code + "_" +System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        return R.ok();
    }

    /**
     * 注册接口
     * @param registerVo 注册实体
     * @return String
     */
    @PostMapping("/register")
    public String register(@Valid RegisterVo registerVo, BindingResult result, RedirectAttributes attributes) {
        // 判断实体校验结果是否验证通过
        if (result.hasErrors()) {
            Map<String, String> map = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (r1, r2) -> r1));
            attributes.addFlashAttribute("errors", map);
            return "redirect:http://auth.gulimall.com/register.html";
        }
        // 实体校验通过，进行后续的注册
        String redisCode = redis.opsForValue().get(AuthConstant.CODE_PREFIX + registerVo.getPhone());
        Map<String, String> error = new HashMap<>(1);
        if (!StringUtils.isEmpty(redisCode)) {
            // 删除redis中的验证码
            redis.delete(AuthConstant.CODE_PREFIX + registerVo.getPhone());
            // 校验提交的验证码是否与redis中的验证码一致
            String code = redisCode.split("_")[0];
            if (code.equals(registerVo.getCode())) {
                // 验证码相等，远程调用会员服务进行注册
                R r = memberFeign.registerMember(registerVo);
                if (r.getCode() == 0) {
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    String msg = (String) r.get("msg");
                    error.put("msg", msg);
                }
            } else {
                error.put("code", "验证码错误");
            }
        } else {
            error.put("code", "验证码不存在");
        }
        attributes.addFlashAttribute("errors", error);
        // 返回注册页
        return "redirect:http://auth.gulimall.com/register.html";
    }

    /**
     * 登录接口
     * @param loginVo 登录实体
     * @param redirectAttributes
     * @return String
     */
    @PostMapping("/login")
    public String login(LoginVo loginVo, RedirectAttributes redirectAttributes, HttpSession httpSession) {
        // 远程调用登录接口
        R r = memberFeign.loginMember(loginVo);
        if (r.getCode() == 0) {
            // 登录成功,跳转到商城首页，并将用户信息存入session中
            String memberJson = JSON.toJSONString(r.get(AuthConstant.USER_SESSION_NAME));
            MemberRespVo memberRespVo = JSON.parseObject(memberJson, MemberRespVo.class);
            // 将用户信息放入session中，将session放到redis中
            httpSession.setAttribute(AuthConstant.USER_SESSION_NAME, memberRespVo);
            return "redirect:http://gulimall.com";
        }
        String msg = (String) r.get("msg");
        Map<String, String> map = new HashMap<>(1);
        map.put("msg", msg);
        redirectAttributes.addFlashAttribute("errors", map);
        return "redirect:http://auth.gulimall.com/login.html";
    }

}
