package com.muke.gulimall.search.controller;

import com.muke.gulimall.search.service.SearchService;
import com.muke.gulimall.search.vo.SearchParam;
import com.muke.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/12 19:52
 */
@Controller
public class SearchController {

    @Resource
    private SearchService searchService;

    /**
     * 检索路由
     * @return
     */
    @GetMapping("/search.html")
    public String searchIndex(SearchParam param, Model model) {
        SearchResult result = searchService.searchProduct(param);
        model.addAttribute("products", result);
        return "search";
    }

}
