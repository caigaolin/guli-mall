package com.muke.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.muke.gulimall.search.config.ElasticConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 需要启动IOC容器
 * @author 木可
 * @version 1.0
 * @date 2021/3/8 18:13
 */
@SpringBootTest
public class TestDemo {

    @Resource(name = "restHighLevelClient")
    private RestHighLevelClient client;

    @Test
    public void esTest() throws IOException {
        // 构建检索请求
        SearchRequest searchRequest = new SearchRequest("new_bank");

        // 构建检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        sourceBuilder.aggregation(AggregationBuilders.terms("age_doc_count").field("age").size(10));
        sourceBuilder.aggregation(AggregationBuilders.avg("balance_avg").field("balance"));
        searchRequest.source(sourceBuilder);

        System.out.println(sourceBuilder.toString());

        // 发送请求，获取结果
        SearchResponse search = client.search(searchRequest, ElasticConfig.COMMON_OPTIONS);
        System.out.println(search.toString());
        // 解析结果

        // 获取hits
        SearchHits hits = search.getHits();
        // 获取结果hits
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            // 将hit转为string
            String sourceAsString = searchHit.getSourceAsString();
            // 将string转为对象
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

        // 获取聚合
        Aggregations aggregations = search.getAggregations();
        Terms ageDocCount = aggregations.get("age_doc_count");
        for (Terms.Bucket bucket : ageDocCount.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println(keyAsString);
        }
        Avg balanceAvg = aggregations.get("balance_avg");
        System.out.println(balanceAvg.getValueAsString());

    }

    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
}
