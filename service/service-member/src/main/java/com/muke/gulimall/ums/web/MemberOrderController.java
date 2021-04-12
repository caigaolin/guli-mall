package com.muke.gulimall.ums.web;

import com.muke.common.utils.R;
import com.muke.gulimall.ums.feign.OrderFeign;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/12 12:34
 */
@Controller
public class MemberOrderController {

    @Resource
    private OrderFeign orderFeign;

    @GetMapping("/memberOrder.html")
    public String toMemberOrderPage(@RequestParam(value = "page", defaultValue = "1") String page, Model model) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("page", page);
        R r = orderFeign.list(map);
        if (r.getCode().equals(0)) {
            Object pageObj = r.get("page");
            model.addAttribute("page", pageObj);
        }
        return "list";
    }

}
