package com.muke.gulimall.search.controller;

import com.muke.common.to.es.SpuUpEsTo;
import com.muke.common.utils.R;
import com.muke.gulimall.search.service.EsSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 16:38
 */
@Slf4j
@RestController
@RequestMapping("es/save")
public class EsSaveController {

    @Resource
    private EsSaveService esSaveService;

    /**
     * 保存商品上架数据
     * @param spuUpEsToList spu上架实体集合
     * @return R
     */
    @PostMapping("/spu")
    public R saveSpuToEs(@RequestBody List<SpuUpEsTo> spuUpEsToList) {
        try {
            boolean isSave = esSaveService.saveSpu(spuUpEsToList);
            if (!isSave) {
                log.error("EsSaveController==》saveSpuToEs:保存商品上架数据失败");
                return R.error();
            }
        }catch (Exception e) {
            log.error("EsSaveController==》saveSpuToEs:保存商品上架数据异常：{ }", e);
            return R.error();
        }
        return R.ok();
    }

}
