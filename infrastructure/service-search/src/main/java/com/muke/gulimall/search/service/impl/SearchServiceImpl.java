package com.muke.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.muke.common.to.es.SpuUpEsTo;
import com.muke.gulimall.search.config.ElasticConfig;
import com.muke.gulimall.search.constant.SearchConstant;
import com.muke.gulimall.search.service.SearchService;
import com.muke.gulimall.search.vo.SearchParam;
import com.muke.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.collect.HppcMaps;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.SearchContextAggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/12 20:07
 */
@Service("searchService")
public class SearchServiceImpl implements SearchService {

    @Resource(name = "restHighLevelClient")
    private RestHighLevelClient restClient;

    /**
     * 根据条件检索商品
     * @param param 检索条件
     * @return SearchResult
     */
    @Override
    public SearchResult searchProduct(SearchParam param) {
        SearchResult result = new SearchResult();
        try {
            // 根据检索条件构建检索资源
            SearchSourceBuilder source = searchQueryBuilder(param);
            System.out.println(source.toString());
            // 构建检索请求
            SearchRequest searchRequest = new SearchRequest(new String[]{SearchConstant.ES_INDEX_NAME}, source);
            // 执行检索，得到结果
            SearchResponse search = restClient.search(searchRequest, ElasticConfig.COMMON_OPTIONS);
            result = parsingSearchResponse(search, param);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建检索请求
     * @param param
     * @return
     */
    private SearchSourceBuilder searchQueryBuilder(SearchParam param) {
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 构建query - bool条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 判断全文检索字段非空
        if (!StringUtils.isEmpty(param.getKeyword())) {
            // 按商品标题进行全文检索
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 判断三级分类id非空
        if (param.getCatalog3Id() != null) {
            // 按分类id检索
            boolQueryBuilder.filter(QueryBuilders.termQuery("catelogId", param.getCatalog3Id()));
        }
        if (param.getHasStock() != null) {
            // 按是否有库存进行检索
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 判断价格区间非空
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            // 构建区间检索条件
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            // 价格区间格式：skuPrice=1_500 / _500 / 500_
            String[] prices = param.getSkuPrice().split("_");
            // 判断起始值非空
            if (!StringUtils.isEmpty(prices[0])) {
                skuPrice.gte(prices[0]);
            }
            // 判断结束值非空
            if (!StringUtils.isEmpty(prices[1])) {
                skuPrice.lte(prices[1]);
            }
            boolQueryBuilder.filter(skuPrice);
        }
        // 判断品牌id集合非空
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 判断属性集合非空
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            // 遍历属性
            for (String attr : param.getAttrs()) {
                // 属性规则： attrs=1_3G:4G:5G
                String[] attrStr = attr.split("_");
                // 得到属性id
                String attrId = attrStr[0];
                // 得到数据值集合
                String[] attrValues = attrStr[1].split(":");
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None));
            }
        }
        builder.query(boolQueryBuilder);

        // 构建sort排序条件
        // 判断排序非空
        if (!StringUtils.isEmpty(param.getSort())) {
            // 排序字段规则：sort=saleCount_desc/asc
            String[] sorts = param.getSort().split("_");
            // 判断排序规则
            if ("desc".equals(sorts[1])) {
                // 降序
                builder.sort(sorts[0], SortOrder.DESC);
            } else {
                builder.sort(sorts[0], SortOrder.ASC);
            }

        }

        // 构建分页条件
        // 计算分页
        // 当前显示记录数
        int count = (param.getPageNum() - 1) * SearchConstant.PAGE_SIZE;
        builder.from(count);
        builder.size(SearchConstant.PAGE_SIZE);

        // 构建检索字段高亮
        // 判断全文检索字段非空
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            HighlightBuilder skuTitleHighlight = highlightBuilder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            builder.highlighter(skuTitleHighlight);
        }

        // 构建聚合分析
        // 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_id_agg").field("brandId").size(16);
        // 品牌子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brandAgg);

        // 分类聚合
        TermsAggregationBuilder cateAgg = AggregationBuilders.terms("category_id_agg").field("catelogId").size(10);
        // 分类子聚合
        cateAgg.subAggregation(AggregationBuilders.terms("category_name_agg").field("catelogName.keyword").size(1));
        builder.aggregation(cateAgg);

        // 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        // 属性子聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(1));
        attrAgg.subAggregation(attrIdAgg);
        builder.aggregation(attrAgg);
        return builder;
    }

    /**
     * 根据检索结果，封装页面需要的结果数据
     * @param search 检索结果
     * @return SearchResult
     */
    private SearchResult parsingSearchResponse(SearchResponse search, SearchParam param) {
        // 构建最终的返回对象
        SearchResult searchResult = new SearchResult();

        SearchHits hits = search.getHits();
        // 计算分页数据
        // 获取总记录数
        int total = (int)hits.getTotalHits().value;
        // 得到总页码
        int totalPages = total % SearchConstant.PAGE_SIZE == 0 ? total / SearchConstant.PAGE_SIZE : (total / SearchConstant.PAGE_SIZE + 1);
        // 计算所有的页码项
        List<Integer> pageNav = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNav.add(i);
        }
        searchResult.setTotal(total);
        searchResult.setPageNum(param.getPageNum());
        searchResult.setTotalPages(totalPages);
        searchResult.setPageNav(pageNav);

        // 获取所有商品hits
        List<SpuUpEsTo> products = new ArrayList<>();
        SearchHit[] productHits = hits.getHits();
        for (SearchHit productHit : productHits) {
            String sourceAsString = productHit.getSourceAsString();
            // 获取到当前的商品数据
            SpuUpEsTo spuUpEsTo = JSON.parseObject(sourceAsString, new TypeReference<SpuUpEsTo>() {
            });
            // 判断只有在检索标题时，才高亮显示
            if (!StringUtils.isEmpty(param.getKeyword())) {
                // 获取高亮字段
                Map<String, HighlightField> highlightFields = productHit.getHighlightFields();
                // 获取高亮字段的代码字段值
                Text[] skuTitles = highlightFields.get("skuTitle").getFragments();
                if (skuTitles != null && skuTitles.length > 0) {
                    StringBuilder skuTitleTemp = new StringBuilder();
                    for (Text skuTitle : skuTitles) {
                        skuTitleTemp.append(skuTitle.toString());
                    }
                    spuUpEsTo.setSkuTitle(skuTitleTemp.toString());
                }
            }

            products.add(spuUpEsTo);
        }
        searchResult.setProducts(products);

        // 获取所有的聚合分析
        Aggregations aggregations = search.getAggregations();
        // 获取品牌聚合
        ParsedLongTerms brandAgg = aggregations.get("brand_id_agg");
        List<? extends Terms.Bucket> brandBuckets = brandAgg.getBuckets();
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brandBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 得到品牌id
            Long brandId = (Long) bucket.getKey();

            // 获取品牌名称的子聚合
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            // 得到品牌名称
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            // 获取品牌图片的子聚合
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            // 得到品牌图片
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();

            // 品牌数据封装
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBranImg(brandImg);
            brandVos.add(brandVo);
        }
        // 封装品牌聚合数据
        searchResult.setBrands(brandVos);

        // 获取分类聚合
        ParsedLongTerms categoryAgg = aggregations.get("category_id_agg");
        List<? extends Terms.Bucket> categoryBuckets = categoryAgg.getBuckets();
        List<SearchResult.CategoryVo> categoryVos = new ArrayList<>();
        for (Terms.Bucket categoryBucket : categoryBuckets) {
            SearchResult.CategoryVo categoryVo = new SearchResult.CategoryVo();
            // 得到分类id
            Long catelogId = (Long) categoryBucket.getKey();
            // 获取分类名称聚合
            ParsedStringTerms categoryNameAgg = categoryBucket.getAggregations().get("category_name_agg");
            // 得到分类名称
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();

            // 封装分类数据
            categoryVo.setCatelogId(catelogId);
            categoryVo.setCatelogName(categoryName);
            categoryVos.add(categoryVo);
        }
        // 封装分类聚合数据
        searchResult.setCatalogs(categoryVos);

        // 获取属性聚合
        ParsedNested attrAgg = aggregations.get("attr_agg");
        // 获取属性id聚合
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (Terms.Bucket attrIdAggBucket : attrIdAggBuckets) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 得到属性id
            Long attId = (Long) attrIdAggBucket.getKey();
            // 获取属性名称聚合
            ParsedStringTerms attrNameAgg = attrIdAggBucket.getAggregations().get("attr_name_agg");
            // 得到属性名称
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            // 获取属性值聚合
            ParsedStringTerms attrValueAgg = attrIdAggBucket.getAggregations().get("attr_value_agg");
            List<? extends Terms.Bucket> attrValueBuckets = attrValueAgg.getBuckets();
            // 构建属性值集合
            List<String> attrValues = new ArrayList<>();
            for (Terms.Bucket attrValueBucket : attrValueBuckets) {
                // 得到属性值
                String attrValue = attrValueBucket.getKeyAsString();
                attrValues.add(attrValue);
            }
            // 封装属性数据
            attrVo.setAttrId(attId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValues(attrValues);
            attrVos.add(attrVo);
        }
        // 封装属性聚合数据
        searchResult.setAttrs(attrVos);

        return searchResult;
    }
}
