package com.muke.gulimall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.exception.RRException;
import com.muke.common.to.SkuStockStatusTo;
import com.muke.common.to.mq.OrderTo;
import com.muke.common.to.mq.SeckillOrderTo;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;
import com.muke.common.utils.R;
import com.muke.common.vo.MemberRespVo;
import com.muke.gulimall.oms.config.AlipayTemplate;
import com.muke.gulimall.oms.constant.OrderConstant;
import com.muke.gulimall.oms.dao.OrderDao;
import com.muke.gulimall.oms.dto.MemberReceiveAddressDTO;
import com.muke.gulimall.oms.dto.OrderCreateDTO;
import com.muke.gulimall.oms.dto.SpuInfoDTO;
import com.muke.gulimall.oms.dto.WareLockDTO;
import com.muke.gulimall.oms.entity.OrderEntity;
import com.muke.gulimall.oms.entity.OrderItemEntity;
import com.muke.gulimall.oms.enums.OrderPayEnum;
import com.muke.gulimall.oms.enums.OrderSourceEnum;
import com.muke.common.enums.OrderStatusEnum;
import com.muke.gulimall.oms.feign.CartFeign;
import com.muke.gulimall.oms.feign.MemberFeign;
import com.muke.gulimall.oms.feign.ProductFeign;
import com.muke.gulimall.oms.feign.WareFeign;
import com.muke.gulimall.oms.interceptor.LoginInterceptor;
import com.muke.gulimall.oms.service.OrderItemService;
import com.muke.gulimall.oms.service.OrderService;
import com.muke.gulimall.oms.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderEntity> threadLocal = new ThreadLocal<>();

    @Resource
    private MemberFeign memberFeign;
    @Resource
    private CartFeign cartFeign;
    @Autowired
    private ThreadPoolExecutor executor;
    @Resource
    private WareFeign wareFeign;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductFeign productFeign;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private AlipayTemplate alipayTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ????????????
     *
     * @return
     */
    @Override
    public OrderInfoVo tradeOrder() throws ExecutionException, InterruptedException {
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        MemberRespVo memberRespVo = LoginInterceptor.threadLocal.get();

        // ??????????????????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> memberFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // ???????????????????????????????????????????????????
            List<OrderMemberAddressVo> memberReceive = memberFeign.getMemberReceive(memberRespVo.getId());
            orderInfoVo.setMemberAddressVos(memberReceive);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // ????????????????????????????????????????????????????????????
            List<CartItemVo> cartItemsByKey = cartFeign.getCartItemsByKey();
            if (cartItemsByKey != null) {
                orderInfoVo.setOrderItemVos(cartItemsByKey);
            }
        }, executor).thenRunAsync(() -> {
            List<CartItemVo> orderItemVos = orderInfoVo.getOrderItemVos();
            List<Long> skuIds = orderItemVos.stream().map(CartItemVo::getSkuId).collect(Collectors.toList());
            // ??????skuId,????????????????????????????????????
            List<SkuStockStatusTo> stockStatus = wareFeign.getSkuStockStatus(skuIds);
            if (!CollectionUtils.isEmpty(stockStatus)) {
                Map<Long, Boolean> booleanMap = stockStatus.stream().collect(Collectors.toMap(SkuStockStatusTo::getSkuId, SkuStockStatusTo::getStockStatus));
                orderInfoVo.setIsStockMap(booleanMap);
            }
        }, executor);

        // ????????????
        orderInfoVo.setIntegral(memberRespVo.getIntegration());

        CompletableFuture.allOf(cartFuture, memberFuture).get();

        // ??????????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        // ???token?????????redis??????key: order:token:1 , value: {uuid}
        stringRedisTemplate.opsForValue().set(OrderConstant.PREVENT_ORDER_REPEAT_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderInfoVo.setToken(token);
        return orderInfoVo;
    }

    /**
     * ????????????
     *
     * @param orderVo
     * @param orderRespVo
     * @return
     */
    @Transactional(rollbackFor = RRException.class)
    @Override
    public OrderRespVo generateOrder(OrderGenerateVo orderVo, OrderRespVo orderRespVo) {
        orderRespVo.setCode(0);
        // ???????????????
        MemberRespVo respVo = LoginInterceptor.threadLocal.get();
        String key = OrderConstant.PREVENT_ORDER_REPEAT_PREFIX + respVo.getId();
        // ??????lua???????????????????????????????????????????????????????????????????????????
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key), orderVo.getToken());
        if (execute == null || execute.equals(0L)) {
            // ????????????
            orderRespVo.setCode(1);
            orderRespVo.setMessage("???????????????????????????");
        } else {
            // ????????????
            OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
            // ????????????
            OrderEntity orderEntity = buildOrder(orderVo);
            orderCreateDTO.setOrderEntity(orderEntity);
            // ???????????????
            List<OrderItemEntity> orderItems = buildOrderItem();
            if (!CollectionUtils.isEmpty(orderItems)) {
                orderCreateDTO.setOrderItems(orderItems);
                // ??????????????????
                computePrice(orderEntity, orderItems);
                // ??????
                if (Math.abs(orderEntity.getPayAmount().subtract(orderVo.getPayPrice()).doubleValue()) < 0.01) {
                    // ????????????,????????????
                    saveOrder(orderCreateDTO);
                    WareLockDTO lockDTO = new WareLockDTO();
                    lockDTO.setOrderSn(orderCreateDTO.getOrderEntity().getOrderSn());
                    // ??????????????????
                    List<WareLockDTO.WareInfo> wareInfoList = orderItems.stream().map(item -> {
                        WareLockDTO.WareInfo wareInfo = new WareLockDTO.WareInfo();
                        wareInfo.setSkuId(item.getSkuId());
                        wareInfo.setNumber(item.getSkuQuantity());
                        return wareInfo;
                    }).collect(Collectors.toList());
                    lockDTO.setWareInfoList(wareInfoList);
                    R r = wareFeign.lockWare(lockDTO);
                    if (r.getCode().equals(0)) {
                        // ??????????????????
                        // ??????????????????????????????
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", orderCreateDTO);
                    } else {
                        orderRespVo.setCode(1);
                        orderRespVo.setMessage("????????????????????????");
                        throw new RRException(CustomizeExceptionEnum.LOCK_STOCK_EX);
                    }
                    orderRespVo.setOrderSn(orderCreateDTO.getOrderEntity().getOrderSn());
                    orderRespVo.setPayPrice(orderCreateDTO.getOrderEntity().getPayAmount());
                } else {
                    orderRespVo.setCode(1);
                    orderRespVo.setMessage("??????????????????");
                }
            } else {
                orderRespVo.setCode(1);
                orderRespVo.setMessage("????????????????????????????????????????????????");
            }
        }
        return orderRespVo;
    }

    /**
     * ????????????
     * @param orderEntity
     */
    @Override
    public void closedOrder(OrderEntity orderEntity) {
        // ?????????????????????????????????????????????????????????
        OrderEntity entity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderEntity.getOrderSn()).eq("status", OrderStatusEnum.WAIT_PAY.getCode()));
        if (entity != null) {
            // ??????????????????
            OrderEntity orderEntity1 = new OrderEntity();
            orderEntity1.setId(entity.getId());
            orderEntity1.setStatus(OrderStatusEnum.CLOSED_ORDER.getCode());
            baseMapper.updateById(orderEntity1);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity, orderTo);
            orderTo.setStatus(orderEntity1.getStatus());
            // ?????????????????????????????????????????????????????????
            rabbitTemplate.convertAndSend("order-event-exchange", "order.closed", orderTo);
        }
    }

    @Override
    public PayVo getPayInfo(String orderSn) {
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        payVo.setOutTradeNo(orderSn);
        payVo.setBody(orderEntity.getNote());
        // ??????????????????12.0001  ==??? 12.01
        payVo.setTotalAmount(orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setSubject(orderEntity.getMemberUsername());
        return payVo;
    }

    @Override
    public Map<String, Object> getOrderListPage(Map<String, Object> params) {
        MemberRespVo respVo = LoginInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", respVo.getId()).orderByDesc("create_time")
        );

        List<OrderCreateDTO> createDTOList = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            OrderCreateDTO createDTO = new OrderCreateDTO();
            createDTO.setOrderEntity(order);
            createDTO.setOrderItems(itemEntities);
            return createDTO;
        }).collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>(16);
        map.put("records", createDTOList);
        map.put("page", page.getPages());
        map.put("size", page.getSize());
        map.put("current", page.getCurrent());
        map.put("total", page.getTotal());
        return map;
    }

    /**
     * ??????????????????
     * @param payAsyncVo
     */
    @Override
    public void updateOrderStatus(PayAsyncVo payAsyncVo) {
        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatusByOrderSn(payAsyncVo.getOut_trade_no(), OrderStatusEnum.FINISHED_ORDER.getCode());
        }

    }

    /**
     * ??????????????????
     * @param orderTo
     */
    @Override
    public void seckillOrder(SeckillOrderTo orderTo) {
        // ??????????????????
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setStatus(OrderStatusEnum.WAIT_PAY.getCode());
        orderEntity.setCreateTime(new Date());
        // ??????????????????
        BigDecimal payAmount = orderTo.getKillPrice().add(new BigDecimal(orderTo.getNum()));
        orderEntity.setPayAmount(payAmount);
        // TODO ???????????????????????????????????????
        orderEntity.setMemberId(orderTo.getMemberId());
        this.save(orderEntity);

        // ?????????????????????
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(orderTo.getOrderSn());
        // TODO ??????skuId????????????????????????
        itemEntity.setSkuId(orderTo.getSkuId());
        itemEntity.setSkuQuantity(orderTo.getNum());
        itemEntity.setRealAmount(payAmount);
        orderItemService.save(itemEntity);
    }


    /**
     * ????????????
     *
     * @param orderCreateDTO
     */
    private void saveOrder(OrderCreateDTO orderCreateDTO) {
        int insert = baseMapper.insert(orderCreateDTO.getOrderEntity());
        if (insert != 1) {
            throw new RRException(CustomizeExceptionEnum.SAVE_ORDER_EX);
        }
        boolean saveBatch = orderItemService.saveBatch(orderCreateDTO.getOrderItems());
        if (!saveBatch) {
            throw new RRException(CustomizeExceptionEnum.SAVE_ORDER_ITEM_EX);
        }
    }

    /**
     * ??????????????????
     *
     * @param orderEntity
     * @param orderItems
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        // ????????????
        BigDecimal totalPrice = new BigDecimal("0.0");
        // ????????????
        BigDecimal promotion = new BigDecimal("0.0");
        // ??????????????????
        BigDecimal integration = new BigDecimal("0.0");
        // ????????????
        BigDecimal coupon = new BigDecimal("0.0");
        for (OrderItemEntity item : orderItems) {
            totalPrice = totalPrice.add(item.getRealAmount());
            promotion = promotion.add(item.getPromotionAmount());
            integration = integration.add(item.getIntegrationAmount());
            coupon = coupon.add(item.getCouponAmount());
        }
        // ????????????
        BigDecimal payPrice = totalPrice.add(orderEntity.getFreightAmount());

        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(payPrice);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
    }

    /**
     * ????????????
     *
     * @param orderVo
     * @return
     */
    private OrderEntity buildOrder(OrderGenerateVo orderVo) {
        MemberRespVo memberRespVo = LoginInterceptor.threadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        //  1.1???????????????
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        //     ???????????????
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setMemberUsername(memberRespVo.getUsername());
        //     ????????????????????????????????????
        orderEntity.setStatus(OrderStatusEnum.WAIT_PAY.getCode());
        //     ???????????????????????????PC
        orderEntity.setSourceType(OrderSourceEnum.PC.getCode());
        //     ??????????????????????????????????????????
        orderEntity.setPayType(OrderPayEnum.ALIPAY.getCode());
        //     ??????????????????
        orderEntity.setCreateTime(new Date());
        //  1.2???????????????????????????
        R memberFeignFare = wareFeign.getFare(orderVo.getAddrId());
        if (memberFeignFare.getCode().equals(0)) {
            // ????????????
            Object data = memberFeignFare.get("data");
            String dataStr = JSON.toJSONString(data);
            MemberReceiveAddressDTO receiveAddressDTO = JSON.parseObject(dataStr, new TypeReference<MemberReceiveAddressDTO>() {
            });
            // ????????????
            orderEntity.setFreightAmount(receiveAddressDTO.getFare());
            orderEntity.setReceiverName(receiveAddressDTO.getMemberAddress().getName());
            orderEntity.setReceiverPhone(receiveAddressDTO.getMemberAddress().getPhone());
            orderEntity.setReceiverProvince(receiveAddressDTO.getMemberAddress().getProvince());
            orderEntity.setReceiverCity(receiveAddressDTO.getMemberAddress().getCity());
            orderEntity.setReceiverDetailAddress(receiveAddressDTO.getMemberAddress().getDetailAddress());
            orderEntity.setReceiverRegion(receiveAddressDTO.getMemberAddress().getRegion());
            orderEntity.setReceiverPostCode(receiveAddressDTO.getMemberAddress().getPostCode());
        }
        // ???order????????????threadLocal
        threadLocal.set(orderEntity);
        return orderEntity;
    }

    /**
     * ???????????????
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItem() {
        OrderEntity orderEntity = threadLocal.get();
        List<CartItemVo> cartItemsByKey = cartFeign.getCartItemsByKey();
        if (!CollectionUtils.isEmpty(cartItemsByKey)) {
            return cartItemsByKey.stream().map(item -> {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setOrderSn(orderEntity.getOrderSn());
                // ??????sku??????
                itemEntity.setSkuId(item.getSkuId());
                itemEntity.setSkuPrice(item.getPrice());
                itemEntity.setSkuName(item.getTitle());
                itemEntity.setSkuPic(item.getImage());
                itemEntity.setSkuQuantity(item.getCount());
                itemEntity.setSkuAttrsVals(String.join(";", item.getSkuAttrs()));
                // ??????spu??????
                R spuInfoR = productFeign.getSpuInfoBySkuId(item.getSkuId());
                if (spuInfoR.getCode().equals(0)) {
                    Object spuInfoObj = spuInfoR.get("spuInfo");
                    String spuInfoStr = JSON.toJSONString(spuInfoObj);
                    SpuInfoDTO spuInfoDTO = JSON.parseObject(spuInfoStr, SpuInfoDTO.class);
                    itemEntity.setSpuId(spuInfoDTO.getId());
                    itemEntity.setSpuName(spuInfoDTO.getSpuName());
                    itemEntity.setSpuBrand(spuInfoDTO.getBrandId().toString());
                    itemEntity.setSpuPic(item.getImage());
                    itemEntity.setCategoryId(spuInfoDTO.getCatalogId());
                }
                // ??????????????????
                itemEntity.setCouponAmount(new BigDecimal("0.0"));
                itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
                itemEntity.setPromotionAmount(new BigDecimal("0.0"));
                BigDecimal originPrice = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
                BigDecimal realPrice = originPrice.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount()).subtract(itemEntity.getPromotionAmount());
                itemEntity.setRealAmount(realPrice);
                // ??????????????????
                itemEntity.setGiftGrowth(new BigDecimal("0.0").intValue());
                itemEntity.setGiftIntegration(new BigDecimal("0.0").intValue());
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }
}