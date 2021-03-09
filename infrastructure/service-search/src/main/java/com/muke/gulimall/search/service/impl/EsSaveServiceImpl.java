package com.muke.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.exception.RRException;
import com.muke.common.to.es.SpuUpEsTo;
import com.muke.gulimall.search.config.ElasticConfig;
import com.muke.gulimall.search.constant.SearchConstant;
import com.muke.gulimall.search.service.EsSaveService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/9 16:43
 */
@Service
public class EsSaveServiceImpl implements EsSaveService {

    @Resource(name = "restHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 保存商品上架数据到ES中
     * @param spuUpEsToList spu上架实体集合
     */
    @Override
    public boolean saveSpu(List<SpuUpEsTo> spuUpEsToList) throws IOException {

        // 构建批量保存请求
        BulkRequest bulkRequest = new BulkRequest();

        for (SpuUpEsTo spuUpEsTo : spuUpEsToList) {
            IndexRequest indexRequest = new IndexRequest(SearchConstant.ES_INDEX_NAME);
            String jsonString = JSON.toJSONString(spuUpEsTo);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        // 执行批量保存
        BulkResponse bulkResponse = client.bulk(bulkRequest, ElasticConfig.COMMON_OPTIONS);

        boolean failures = bulkResponse.hasFailures();
        return !failures;
    }
}
