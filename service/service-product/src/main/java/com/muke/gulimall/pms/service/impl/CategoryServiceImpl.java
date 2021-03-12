package com.muke.gulimall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.muke.gulimall.pms.help.CategoryHelp;
import com.muke.gulimall.pms.service.CategoryBrandRelationService;
import com.muke.gulimall.pms.vo.web.Catelog2Vo;
import com.muke.gulimall.pms.vo.web.Catelog3Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.pms.dao.CategoryDao;
import com.muke.gulimall.pms.entity.CategoryEntity;
import com.muke.gulimall.pms.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryHelp categoryHelp;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询以及一级分类
     *   1.每一个需要缓存的数据我们都来指定要放到那个名字的缓存【缓存分区（按照业务类型划分）】
     *   2.@Cacheable(value = {"category"})
     *       代表当前方法的结果需要缓存，如果缓存中有，方法不用调用
     *       如果缓存总没有，会调用方法，最后将方法的结果放入缓存
     *   3.默认行为
     *       如果缓存中有，方法不用调用
     *       key默认自动生成，缓存的名字：category::SimpleKey [] 【自动生成的key值】
     *       缓存的value值，默认使用JDK序列化机制，将序列化后的数据存到redis
     *       默认ttl时间：-1
     *   4.自定义
     *       指定生成缓存使用的key；key属性的指定，接受一个 SpEL
     *       指定缓存数据的有效期；配置文件中设置 ttl
     *       将数据保存为json格式
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public List<CategoryEntity> selectCatelog1Level() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    /**
     * 获取树形分类数据
     *
     * @return List<CategoryEntity>
     */
    @Override
    public List<CategoryEntity> getCategoryTree() {
        // 分类列表数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 获取树形分类数据
        return entities.stream()
                .filter(item -> item.getParentCid() == 0)
                .peek(item -> {
                    item.setChildren(categoryHelp.getCategoryChildren(item, entities));
                })
                .sorted(Comparator.comparingInt(cate -> (cate.getSort() == null ? 0 : cate.getSort())))
                .collect(Collectors.toList());
    }

    /**
     * 删除没有引用的分类
     *
     * @param catIds 分类id数组
     */
    @Override
    public void deleteBatchCate(Long[] catIds) {
        // TODO 判断该分类是否有关联数据

        // 删除分类
        baseMapper.deleteBatchIds(Arrays.asList(catIds));
    }

    /**
     * 修改分类关联数据
     *      清空category缓存分区中的所有缓存数据
     *
     * @param category 分类实体

     1.同时进行多种缓存操作：@Caching
    2.指定删除某个分区下的所有数据 @CacheEvict
    3.存储统一类型的数据，都可以指定成同一个分区。

    使用组合注解，清空缓存分区中的缓存
    @Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getCateDataByDB2'"),
            @CacheEvict(value = {"category"}, key = "'selectCatelog1Level'")
    })*/
    @CacheEvict(value = {"category"}, allEntries = true) // 清空缓存分区中的全部缓存数据
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCateRelation(CategoryEntity category) {
        baseMapper.updateById(category);

        if (!StringUtils.isEmpty(category.getName())) {
            // 修改关联数据
            categoryBrandRelationService.updateCateRelationData(category.getCatId(), category.getName());
        }
    }

    /**
     * 查询分类使用缓存注解
     *
     * @return Map<String, List < Catelog2Vo>>
     */
    public Map<String, List<Catelog2Vo>> getCateDataByDB2() {
        System.out.println("正在查询数据库。。。。");
        // 获取到所有分类数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 构建最终的返回集合
        Map<String, List<Catelog2Vo>> cateMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(categoryEntities)) {
            for (CategoryEntity cate1 : categoryEntities) {
                // 判断一级分类
                if (cate1.getParentCid().equals(0L)) {
                    // 构建二级分类集合
                    List<Catelog2Vo> catelog2Vos = new ArrayList<>();
                    for (CategoryEntity cate2 : categoryEntities) {
                        // 判断二级分类
                        if (cate2.getParentCid().equals(cate1.getCatId())) {
                            Catelog2Vo catelog2Vo = new Catelog2Vo();
                            catelog2Vo.setCatalog1Id(cate1.getCatId().toString());
                            catelog2Vo.setId(cate2.getCatId().toString());
                            catelog2Vo.setName(cate2.getName());
                            // 构建三级分类集合
                            List<Catelog3Vo> catelog3Vos = new ArrayList<>();
                            for (CategoryEntity cate3 : categoryEntities) {
                                // 判断三级分类
                                if (cate3.getParentCid().equals(cate2.getCatId())) {
                                    Catelog3Vo catelog3Vo = new Catelog3Vo();
                                    catelog3Vo.setCatalog2Id(cate2.getCatId().toString());
                                    catelog3Vo.setId(cate3.getCatId().toString());
                                    catelog3Vo.setName(cate3.getName());
                                    catelog3Vos.add(catelog3Vo);
                                }
                            }
                            catelog2Vo.setCatalog3List(catelog3Vos);
                            catelog2Vos.add(catelog2Vo);
                        }
                    }
                    cateMap.put(cate1.getCatId().toString(), catelog2Vos);
                }
            }
        }
        return cateMap;
    }

    /**
     * 查询分类
     *
     * @return Map<String, List < Catelog2Vo>>
     */
    private Map<String, List<Catelog2Vo>> getCateDataByDB() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        // 查询DB前，先获取缓存中是否存在数据
        String catelog2Json = ops.get("catelog2Json");
        if (!StringUtils.isEmpty(catelog2Json)) {
            return JSON.parseObject(catelog2Json, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("正在查询数据库。。。。");
        // 获取到所有分类数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 构建最终的返回集合
        Map<String, List<Catelog2Vo>> cateMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(categoryEntities)) {
            for (CategoryEntity cate1 : categoryEntities) {
                // 判断一级分类
                if (cate1.getParentCid().equals(0L)) {
                    // 构建二级分类集合
                    List<Catelog2Vo> catelog2Vos = new ArrayList<>();
                    for (CategoryEntity cate2 : categoryEntities) {
                        // 判断二级分类
                        if (cate2.getParentCid().equals(cate1.getCatId())) {
                            Catelog2Vo catelog2Vo = new Catelog2Vo();
                            catelog2Vo.setCatalog1Id(cate1.getCatId().toString());
                            catelog2Vo.setId(cate2.getCatId().toString());
                            catelog2Vo.setName(cate2.getName());
                            // 构建三级分类集合
                            List<Catelog3Vo> catelog3Vos = new ArrayList<>();
                            for (CategoryEntity cate3 : categoryEntities) {
                                // 判断三级分类
                                if (cate3.getParentCid().equals(cate2.getCatId())) {
                                    Catelog3Vo catelog3Vo = new Catelog3Vo();
                                    catelog3Vo.setCatalog2Id(cate2.getCatId().toString());
                                    catelog3Vo.setId(cate3.getCatId().toString());
                                    catelog3Vo.setName(cate3.getName());
                                    catelog3Vos.add(catelog3Vo);
                                }
                            }
                            catelog2Vo.setCatalog3List(catelog3Vos);
                            catelog2Vos.add(catelog2Vo);
                        }
                    }
                    cateMap.put(cate1.getCatId().toString(), catelog2Vos);
                }
            }
        }
        // 将查询结果以JSON的形式保存到缓存中
        ops.set("catelog2Json", JSON.toJSONString(cateMap));
        return cateMap;
    }

    /**
     * 获取二级及三级分类数据 ==>使用本地锁
     *
     * @return List<Map < String, List < Catelog2Vo>>>
     */
    public Map<String, List<Catelog2Vo>> selectCatelog2DataWithLocalLock() {
        // 给DB操作加本地锁
        // 注意：加锁需要将查DB和存入缓存写在一个原子操作中
        synchronized (this) {
            // 获取到所有分类数据
            return getCateDataByDB2();
        }
    }

    /**
     * 获取二级及三级分类数据 ==》使用基础分布式锁
     * 使用redis做分布式锁
     * 两个核心点：加锁时保证原子性和解锁时保证原子性
     *
     * @return List<Map < String, List < Catelog2Vo>>>
     */
    public Map<String, List<Catelog2Vo>> selectCatelog2DataWithRedisLock() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        // redis中 setnx 命令可用来实现分布式锁
        String uuid = UUID.randomUUID().toString();
        // 大量并发过来，进行竞争锁
        Boolean lock = ops.setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);

        // 判断哪个线程获取到了锁
        if (lock) {
            Map<String, List<Catelog2Vo>> result = new HashMap<>();
            try {
                // 查询数据库
                result = getCateDataByDB2();
            } catch (Exception e) {

            } finally {
                // 删除锁，并且需要释放当前线程的锁，可以使用redis提供的Lua脚本删除锁，其它方式都不能保证删锁操作的原子性
                String luaScript = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                RedisOperations<String, String> operations = ops.getOperations();
                // 执行脚本
                Long execute = operations.execute(new DefaultRedisScript<Long>(luaScript, Long.class), Collections.singletonList("lock"), uuid);
            }
            return result;
        } else {
            // 竞争失败，则继续竞争
            try {
                // 让当前线程修改300毫秒
                Thread.sleep(300);
            } catch (Exception e) {

            } finally {
                selectCatelog2DataWithRedisLock();
            }
            return null;
        }
    }

    /**
     * 获取二级及三级分类数据 ==》使用redisson分布式锁
     * 使用redis做分布式锁
     * 两个核心点：加锁时保证原子性和解锁时保证原子性
     *
     * @return List<Map < String, List < Catelog2Vo>>>
     */
    public Map<String, List<Catelog2Vo>> selectCatelog2DataWithRedissonLock() {
        // 获取锁
        RLock lock = redissonClient.getLock("catelog-json-lock");
        // 加锁
        lock.lock();

        Map<String, List<Catelog2Vo>> result = new HashMap<>();
        try {
            // 查询数据库
            result = getCateDataByDB2();
        } catch (Exception e) {

        } finally {
            // 释放锁
            lock.unlock();
        }
        return result;

    }


    /**
     * 从缓存中获取三级分类数据
     * 缓存三大问题：
     * 1.缓存穿透：查询DB中不存在地数据，缓存null结果
     * 2.缓存雪崩：大量key同时到期，给缓存过期时间（一个固定时间+随机值）
     * 3.缓存击穿：热点key在高并发前过期，加锁
     *
     * @return Map<String, List < Catelog2Vo>>
     */
    public Map<String, List<Catelog2Vo>> selectCatelog2Cache2() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        // 获取缓存中的三级分类
        String cate2Str = ops.get("catelog2Json");
        // 判断非空
        if (StringUtils.isEmpty(cate2Str)) {
            System.out.println("缓存未命中，将要查询数据库");
            // 缓存中没有，则需要查询数据库
            return selectCatelog2DataWithRedissonLock();
        }
        System.out.println("缓存命中");
        return JSON.parseObject(cate2Str, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * 从缓存中获取三级分类数据
     * 缓存三大问题：
     * 1.缓存穿透：查询DB中不存在地数据，缓存null结果
     * 2.缓存雪崩：大量key同时到期，给缓存过期时间（一个固定时间+随机值）
     * 3.缓存击穿：热点key在高并发前过期，加锁
     *
     * @return Map<String, List < Catelog2Vo>>
     */
    @Cacheable(value = {"category"}, key = "'getCateDataByDB2'")
    @Override
    public Map<String, List<Catelog2Vo>> selectCatelog2Cache() {
        return selectCatelog2DataWithRedissonLock();
    }

}