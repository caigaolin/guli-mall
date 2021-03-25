package com.muke.gulimall.search.service;

import com.muke.gulimall.search.vo.SearchParam;
import com.muke.gulimall.search.vo.SearchResult;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/12 20:07
 */
public interface SearchService {
    /**
     * 检索商品
     * @param param 检索条件
     * @return SearchResult
     */
    SearchResult searchProduct(SearchParam param);
}
