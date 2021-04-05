package com.muke.gulimall.oms.web;

import com.muke.common.exception.RRException;
import com.muke.gulimall.oms.entity.OrderEntity;
import com.muke.gulimall.oms.service.OrderService;
import com.muke.gulimall.oms.vo.OrderGenerateVo;
import com.muke.gulimall.oms.vo.OrderInfoVo;
import com.muke.gulimall.oms.vo.OrderRespVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.rmi.runtime.Log;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/28 11:28
 */
@Controller
public class OrderWebController {
    @Resource
    private OrderService orderService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String toOrderPage(@PathVariable("page") String page) {

        return page;
    }

    @GetMapping("/send/message")
    @ResponseBody
    public String sendMessage() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", orderEntity);
        return "OK!!!";
    }

    /**
     * 结算购物车,跳转至订单确认页
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderInfoVo orderInfoVo = orderService.tradeOrder();
        model.addAttribute("order", orderInfoVo);

        return "confirm";
    }


    /**
     * 生成订单
     * @param orderVo 订单参数
     * @return
     */
    @PostMapping("/generate-order")
    public String generateOrder(OrderGenerateVo orderVo, Model model, RedirectAttributes redirectAttributes) {
        OrderRespVo respVo = new OrderRespVo();
        try {
            respVo = orderService.generateOrder(orderVo, respVo);
            if (respVo.getCode().equals(0)) {
                // 订单生成成功
                model.addAttribute("orderSn", respVo.getOrderSn());
                model.addAttribute("payPrice", respVo.getPayPrice());
                return "pay";
            }
        } catch (RRException e) {
        }
        redirectAttributes.addFlashAttribute("msg", respVo.getMessage());
        return "redirect:http://order.gulimall.com/toTrade";

    }
}
