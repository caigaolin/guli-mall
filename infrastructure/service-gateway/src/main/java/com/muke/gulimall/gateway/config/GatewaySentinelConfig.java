package com.muke.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 网关限流回调配置
 * @author 木可
 * @version 1.0
 * @date 2021/4/19 22:35
 */
@Configuration
public class GatewaySentinelConfig {

    public GatewaySentinelConfig() {
        // 重新设置返回数据格式
        GatewayCallbackManager.setBlockHandler((serverWebExchange, throwable) -> ServerResponse
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(R.error(CustomizeExceptionEnum.GATEWAY_EX)));
    }
}
