package com.muke.gulimall.oms.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.muke.gulimall.oms.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    /**
     * 在支付宝创建的应用的id
     */
    private   String appId = "2021000117637225";

    /**
     * 商户私钥，您的PKCS8格式RSA2私钥
     */
    private  String merchantPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNnFVZ14VEU3sx7YaqGfuA2fmtuW4c6B8dYetJgGdY8cOHjElikeB45PRL+SLzj5QGx4tbrelKv2yYY2AVKoA1r5IFNR84XZGWorpo63SGIkATf1Amn9gya7eRICU7ucZcEhb20rzxtZkJjBTvup2ktFRw9pN7xKyHn1Do5uotDsb6NK5YMnxlRqYXyyW/chmBi2jq6/k1nEEOeXcXpgMGw/qm9q+Tf758vmvAfRxC+jWEMoFWhZ3Hp2RMRRl3PLOm8rDzYM+LNyVk18Dn8XIQieX6IdNYML51eHOHU5phDoOas7F5GonC5Lk3SqmcDqU3XEaD8Y2xNbudwQrKfa2nAgMBAAECggEAduLElQRnEZG0X5o18CCYEPjusHZ4hzQoyxYl3jM6kGWH7ghMo8AsX9J3dkDWovvrHjXwPSuoK+TBWr4zMBHaSTf4sv6CE2QakghTzzm9Pc8LVFMSCxsu+kWMev4txBKXATz+ooERtyrqLCW6ffiB2IC8U4nvD31yCvophx41g1Ufz4098eod8YY3vRl80LKlgw2PQ4rU44W9H8JnlPYuhtH2OKz4Zcqc81CRQu6O0UwQxZ2+lx39yQZH+xU8bJlqGjGwG+7/WI+mJvEXfnvle16acDlN+937Cut8SlUqa0jNtIcRnj8nM2UWo4K26aNEr/+dfv/Nlxc/R6Ba7UUzYQKBgQDO+y1b+bBDR37OTFI34Ryv1fskNVxohvqmFas2Q1wMmVaPwFfSVdXDeY6PYcuoXThOXhE7sJl959whigWEggrMtH3HeTJTS/Uf92dUqHrj7uAnCab2St39KrZVQdl8vleXiIi08ejqoCsTXS2Grzp+XHsMz5BIOGFLkTyW2+/JKwKBgQCvJeJjxldq4ootbdnsRr8Fs0qmoym4tFBrW1+5HiY/61TTuggIRra3ca0FGBBOlRBEFiZQlm9ZcaH52f2pm6DVC3PqNtS+lIpCEe4rOLqGNNnd1vvGRCcUQey5M/T3WY6DHXFdZPa/On2ZFPAtt9T1uWpBGYuqWdtGkb1WCMu3dQKBgHjhow0bV0EDyWAsnLv74egjnYf5Dh4R7uL+tr57OpA1IaPU3rV94dlG7eZ5ZBrRTqgeMgxdSFeLCTohRLWtySbipq7P7n9NVjy0HgoIV7U09n52Gaev89P+lPxX9+/trnRWjhi3S6NzpZSqj4MgZZYU5tyFgKtdGh2rFeq5SmJ/AoGBAIfzShTUQcqPaV315K/IAkI0dv99qsbZ/4o1dMnGIz5+NmvZ/J4f9XfS3tATa6Tzg5q17qui+W9eCrJbiM/8Iwz7RNBKGzyLI/5A2LPn5rCBmZLearPs9sB8o3jmYYEVnM8dLX+m/PhWOGN4oy15ge3yLA23/HcQJGpUebisPyR5AoGAadCnpFWY2uwkGsgdlkmijgUUgkowbV2anRcJTFNhTByxgpd1mJuvpnrFSERXshUDBU4boC7czdu3Lsr519GNlUUxivflfvP3p+aJO416ToPw+us38eGLtUO8WQhYEB+3H6GD6AoB6BNYnUUjJTTYnRMWrmQcCn2fHCpxZh5Xeck=";
    /**
     * 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
     */
    private  String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2JfuGmC7UlfVOI6AOWXYJyc3AyBqiYMjjzf3Ai/QmBnkdr0SsLNuQKx5MJ+MsLZRGDRFNZop30a6KCRF7UgglGj2ODdwBRTUqMGTNXhEb21AwSgBcbt+dHt3KRfgr5KDMRHluDCeQyzEi2kKCENHq0+xEXf2JBSgF4JDYVxrtVDvEqV7cZXo+7kuzhJsCFJJ84AxL5CoeKbcP0wTASLbW0p21GjW9c2bYKSilK5owKqN0E2F+ltu3ddK8uJ6IrN4wcxg9kVNml7UCfUL+taAC1QrGFvUx/vunbIkuLqxNRafExkxuoO73vebsm6oHfGmEvhBfGSiEMMTtovFs7+39wIDAQAB";

    /**
     * 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
     */
    private  String notifyUrl = "http://gu6a660znq.52http.net/payed/aliyun";

    /**
     * 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 同步通知，支付成功，一般跳转到成功页
     */
    private  String returnUrl = "http://member.gulimall.com/memberOrder.html";

    /**
     * 签名方式
     */
    private  String signType = "RSA2";

    /**
     * 字符编码格式
     */
    private  String charset = "utf-8";

    /**
     * 支付过期时间
     */
    private String timeout = "5m";

    /**
     * 支付宝网关； https://openapi.alipaydev.com/gateway.do
     */
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                appId, merchantPrivateKey, "json",
                charset, alipayPublicKey, signType);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(notifyUrl);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String outTradeNo = vo.getOutTradeNo();
        //付款金额，必填
        String totalAmount = vo.getTotalAmount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ outTradeNo +"\","
                + "\"total_amount\":\""+ totalAmount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        return alipayClient.pageExecute(alipayRequest).getBody();
    }
}
