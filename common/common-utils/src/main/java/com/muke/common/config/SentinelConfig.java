package com.muke.common.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * sentinel配置
 * @author 木可
 * @version 1.0
 * @date 2021/4/19 17:15
 */
@Configuration
public class SentinelConfig implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        R error = R.error(CustomizeExceptionEnum.TOO_MANY_REQUEST);
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        httpServletResponse.setContentType("application/json");
        httpServletResponse.getWriter().write(JSON.toJSONString(error));
    }

}
