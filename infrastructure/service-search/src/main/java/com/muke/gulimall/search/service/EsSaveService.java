package com.muke.gulimall.search.service;

import com.muke.common.to.es.SpuUpEsTo;

import java.io.IOException;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 16:43
 */
public interface EsSaveService {
    boolean saveSpu(List<SpuUpEsTo> spuUpEsToList) throws IOException;
}
