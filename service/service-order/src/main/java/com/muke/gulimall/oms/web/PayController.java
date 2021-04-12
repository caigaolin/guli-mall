package com.muke.gulimall.oms.web;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.muke.gulimall.oms.config.AlipayTemplate;
import com.muke.gulimall.oms.entity.OrderEntity;
import com.muke.gulimall.oms.entity.PaymentInfoEntity;
import com.muke.gulimall.oms.service.OrderService;
import com.muke.gulimall.oms.service.PaymentInfoService;
import com.muke.gulimall.oms.vo.PayAsyncVo;
import com.muke.gulimall.oms.vo.PayVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/11 22:11
 */
@Controller
public class PayController {

    @Resource
    private AlipayTemplate alipayTemplate;

    @Resource
    private OrderService orderService;

    @Resource
    private PaymentInfoService paymentInfoService;

    /**
     * 前往支付宝支付页面
     * produces = text/html ==> 返回的数据以html的形式显示
     * @param orderSn
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/pay/{orderSn}", produces = "text/html")
    public String toAliPay(@PathVariable("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getPayInfo(orderSn);
        return alipayTemplate.pay(payVo);
    }

    /**
     * 监听支付宝支付成功通知回调
     * @return
     */
    @ResponseBody
    @PostMapping("/payed/aliyun")
    public String payedListener(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {
        Map<String,String> params = new HashMap<>(16);
        Map<String,String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                alipayTemplate.getAlipayPublicKey(), alipayTemplate.getCharset(), alipayTemplate.getSignType());

        if (signVerified) {
            // 验证签名通过，修改订单状态
            orderService.updateOrderStatus(payAsyncVo);
            // 添加一条流水记录
            PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
            paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
            paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
            paymentInfoEntity.setTotalAmount(payAsyncVo.getTotal_amount());
            paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
            paymentInfoEntity.setCreateTime(payAsyncVo.getGmt_create());
            paymentInfoService.save(paymentInfoEntity);
            return "success";
        } else {
            return "";
        }
    }

}
