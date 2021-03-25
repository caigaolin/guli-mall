package com.muke.gulimall.pms;

import com.muke.gulimall.pms.dao.AttrGroupDao;
import com.muke.gulimall.pms.dao.SkuInfoDao;
import com.muke.gulimall.pms.service.SkuInfoService;
import com.muke.gulimall.pms.vo.web.ItemSkuInfoVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/11 15:05
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductTest {

    @Autowired
    private RedissonClient client;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private SkuInfoDao skuInfoDao;

    @Test
    public void reddissonTest() {
        System.out.println(client);
    }

    @Test
    public void itemTest() {
        List<ItemSkuInfoVo.SpuItemBaseAttr> itemSpuBaseAttr = attrGroupDao.getItemSpuBaseAttr(3L, 225L);
        System.out.println(itemSpuBaseAttr);
    }

    @Test
    public void itemSaleTest() {
        List<ItemSkuInfoVo.SkuItemSaleAttr> itemSkuSaleAttr = skuInfoDao.getItemSkuSaleAttr(3L);
        System.out.println(itemSkuSaleAttr);
    }
}
