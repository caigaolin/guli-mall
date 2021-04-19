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
     * 生成订单
     *
     * @return
     */
    @Override
    public OrderInfoVo tradeOrder() throws ExecutionException, InterruptedException {
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        MemberRespVo memberRespVo = LoginInterceptor.threadLocal.get();

        // 获取到当前主线程请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> memberFuture = CompletableFuture.runAsync(() -> {
            // 将主线程中的请求参数设置到子线程中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 远程调用会员服务，查询会员地址信息
            List<OrderMemberAddressVo> memberReceive = memberFeign.getMemberReceive(memberRespVo.getId());
            orderInfoVo.setMemberAddressVos(memberReceive);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 将主线程中的请求参数设置到子线程中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 远程调用购物车服务，获取购物车已选商品项
            List<CartItemVo> cartItemsByKey = cartFeign.getCartItemsByKey();
            if (cartItemsByKey != null) {
                orderInfoVo.setOrderItemVos(cartItemsByKey);
            }
        }, executor).thenRunAsync(() -> {
            List<CartItemVo> orderItemVos = orderInfoVo.getOrderItemVos();
            List<Long> skuIds = orderItemVos.stream().map(CartItemVo::getSkuId).collect(Collectors.toList());
            // 根据skuId,远程调用库存服务查询库存
            List<SkuStockStatusTo> stockStatus = wareFeign.getSkuStockStatus(skuIds);
            if (!CollectionUtils.isEmpty(stockStatus)) {
                Map<Long, Boolean> booleanMap = stockStatus.stream().collect(Collectors.toMap(SkuStockStatusTo::getSkuId, SkuStockStatusTo::getStockStatus));
                orderInfoVo.setIsStockMap(booleanMap);
            }
        }, executor);

        // 设置积分
        orderInfoVo.setIntegral(memberRespVo.getIntegration());

        CompletableFuture.allOf(cartFuture, memberFuture).get();

        // 生成防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        // 将token保存到redis中，key: order:token:1 , value: {uuid}
        stringRedisTemplate.opsForValue().set(OrderConstant.PREVENT_ORDER_REPEAT_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderInfoVo.setToken(token);
        return orderInfoVo;
    }

    /**
     * 生成订单
     *
     * @param orderVo
     * @param orderRespVo
     * @return
     */
    @Transactional(rollbackFor = RRException.class)
    @Override
    public OrderRespVo generateOrder(OrderGenerateVo orderVo, OrderRespVo orderRespVo) {
        orderRespVo.setCode(0);
        // 校验防重码
        MemberRespVo respVo = LoginInterceptor.threadLocal.get();
        String key = OrderConstant.PREVENT_ORDER_REPEAT_PREFIX + respVo.getId();
        // 使用lua脚本进行校验，否则不能保证获取、校验、删除的原子性
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key), orderVo.getToken());
        if (execute == null || execute.equals(0L)) {
            // 校验失败
            orderRespVo.setCode(1);
            orderRespVo.setMessage("订单过期请重新提交");
        } else {
            // 校验成功
            OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
            // 构建订单
            OrderEntity orderEntity = buildOrder(orderVo);
            orderCreateDTO.setOrderEntity(orderEntity);
            // 构建订单项
            List<OrderItemEntity> orderItems = buildOrderItem();
            if (!CollectionUtils.isEmpty(orderItems)) {
                orderCreateDTO.setOrderItems(orderItems);
                // 计算价格相关
                computePrice(orderEntity, orderItems);
                // 验价
                if (Math.abs(orderEntity.getPayAmount().subtract(orderVo.getPayPrice()).doubleValue()) < 0.01) {
                    // 验价成功,保存订单
                    saveOrder(orderCreateDTO);
                    WareLockDTO lockDTO = new WareLockDTO();
                    lockDTO.setOrderSn(orderCreateDTO.getOrderEntity().getOrderSn());
                    // 远程锁定库存
                    List<WareLockDTO.WareInfo> wareInfoList = orderItems.stream().map(item -> {
                        WareLockDTO.WareInfo wareInfo = new WareLockDTO.WareInfo();
                        wareInfo.setSkuId(item.getSkuId());
                        wareInfo.setNumber(item.getSkuQuantity());
                        return wareInfo;
                    }).collect(Collectors.toList());
                    lockDTO.setWareInfoList(wareInfoList);
                    R r = wareFeign.lockWare(lockDTO);
                    if (r.getCode().equals(0)) {
                        // 扣积分等操作
                        // 发送订单生成成功消息
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", orderCreateDTO);
                    } else {
                        orderRespVo.setCode(1);
                        orderRespVo.setMessage("订单锁定库存失败");
                        throw new RRException(CustomizeExceptionEnum.LOCK_STOCK_EX);
                    }
                    orderRespVo.setOrderSn(orderCreateDTO.getOrderEntity().getOrderSn());
                    orderRespVo.setPayPrice(orderCreateDTO.getOrderEntity().getPayAmount());
                } else {
                    orderRespVo.setCode(1);
                    orderRespVo.setMessage("订单验价失败");
                }
            } else {
                orderRespVo.setCode(1);
                orderRespVo.setMessage("生成订单项失败，请确认购买的商品");
            }
        }
        return orderRespVo;
    }

    /**
     * 取消订单
     * @param orderEntity
     */
    @Override
    public void closedOrder(OrderEntity orderEntity) {
        // 只有订单状态为未付款的订单才能关闭订单
        OrderEntity entity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderEntity.getOrderSn()).eq("status", OrderStatusEnum.WAIT_PAY.getCode()));
        if (entity != null) {
            // 修改订单状态
            OrderEntity orderEntity1 = new OrderEntity();
            orderEntity1.setId(entity.getId());
            orderEntity1.setStatus(OrderStatusEnum.CLOSED_ORDER.getCode());
            baseMapper.updateById(orderEntity1);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity, orderTo);
            orderTo.setStatus(orderEntity1.getStatus());
            // 修改订单状态成功，则发送消息，解锁库存
            rabbitTemplate.convertAndSend("order-event-exchange", "order.closed", orderTo);
        }
    }

    @Override
    public PayVo getPayInfo(String orderSn) {
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        payVo.setOutTradeNo(orderSn);
        payVo.setBody(orderEntity.getNote());
        // 设置价格如：12.0001  ==》 12.01
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
     * 修改订单状态
     * @param payAsyncVo
     */
    @Override
    public void updateOrderStatus(PayAsyncVo payAsyncVo) {
        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatusByOrderSn(payAsyncVo.getOut_trade_no(), OrderStatusEnum.FINISHED_ORDER.getCode());
        }

    }

    /**
     * 生成秒杀订单
     * @param orderTo
     */
    @Override
    public void seckillOrder(SeckillOrderTo orderTo) {
        // 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setStatus(OrderStatusEnum.WAIT_PAY.getCode());
        orderEntity.setCreateTime(new Date());
        // 计算应付金额
        BigDecimal payAmount = orderTo.getKillPrice().add(new BigDecimal(orderTo.getNum()));
        orderEntity.setPayAmount(payAmount);
        // TODO 远程查询会员的默认收货地址
        orderEntity.setMemberId(orderTo.getMemberId());
        this.save(orderEntity);

        // 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(orderTo.getOrderSn());
        // TODO 根据skuId远程查询商品信息
        itemEntity.setSkuId(orderTo.getSkuId());
        itemEntity.setSkuQuantity(orderTo.getNum());
        itemEntity.setRealAmount(payAmount);
        orderItemService.save(itemEntity);
    }


    /**
     * 保存订单
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
     * 计算价格相关
     *
     * @param orderEntity
     * @param orderItems
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        // 订单总额
        BigDecimal totalPrice = new BigDecimal("0.0");
        // 促销总额
        BigDecimal promotion = new BigDecimal("0.0");
        // 积分抵扣金额
        BigDecimal integration = new BigDecimal("0.0");
        // 优惠金额
        BigDecimal coupon = new BigDecimal("0.0");
        for (OrderItemEntity item : orderItems) {
            totalPrice = totalPrice.add(item.getRealAmount());
            promotion = promotion.add(item.getPromotionAmount());
            integration = integration.add(item.getIntegrationAmount());
            coupon = coupon.add(item.getCouponAmount());
        }
        // 应付总额
        BigDecimal payPrice = totalPrice.add(orderEntity.getFreightAmount());

        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(payPrice);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
    }

    /**
     * 构建订单
     *
     * @param orderVo
     * @return
     */
    private OrderEntity buildOrder(OrderGenerateVo orderVo) {
        MemberRespVo memberRespVo = LoginInterceptor.threadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        //  1.1生成订单号
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        //     设置用户名
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setMemberUsername(memberRespVo.getUsername());
        //     设置订单状态，默认待付款
        orderEntity.setStatus(OrderStatusEnum.WAIT_PAY.getCode());
        //     设置订单来源，默认PC
        orderEntity.setSourceType(OrderSourceEnum.PC.getCode());
        //     设置订单支付方式，默认支付宝
        orderEntity.setPayType(OrderPayEnum.ALIPAY.getCode());
        //     设置创建时间
        orderEntity.setCreateTime(new Date());
        //  1.2设置订单收货人信息
        R memberFeignFare = wareFeign.getFare(orderVo.getAddrId());
        if (memberFeignFare.getCode().equals(0)) {
            // 调用成功
            Object data = memberFeignFare.get("data");
            String dataStr = JSON.toJSONString(data);
            MemberReceiveAddressDTO receiveAddressDTO = JSON.parseObject(dataStr, new TypeReference<MemberReceiveAddressDTO>() {
            });
            // 设置运费
            orderEntity.setFreightAmount(receiveAddressDTO.getFare());
            orderEntity.setReceiverName(receiveAddressDTO.getMemberAddress().getName());
            orderEntity.setReceiverPhone(receiveAddressDTO.getMemberAddress().getPhone());
            orderEntity.setReceiverProvince(receiveAddressDTO.getMemberAddress().getProvince());
            orderEntity.setReceiverCity(receiveAddressDTO.getMemberAddress().getCity());
            orderEntity.setReceiverDetailAddress(receiveAddressDTO.getMemberAddress().getDetailAddress());
            orderEntity.setReceiverRegion(receiveAddressDTO.getMemberAddress().getRegion());
            orderEntity.setReceiverPostCode(receiveAddressDTO.getMemberAddress().getPostCode());
        }
        // 将order信息放入threadLocal
        threadLocal.set(orderEntity);
        return orderEntity;
    }

    /**
     * 构建订单项
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
                // 设置sku信息
                itemEntity.setSkuId(item.getSkuId());
                itemEntity.setSkuPrice(item.getPrice());
                itemEntity.setSkuName(item.getTitle());
                itemEntity.setSkuPic(item.getImage());
                itemEntity.setSkuQuantity(item.getCount());
                itemEntity.setSkuAttrsVals(String.join(";", item.getSkuAttrs()));
                // 设置spu信息
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
                // 设置优惠信息
                itemEntity.setCouponAmount(new BigDecimal("0.0"));
                itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
                itemEntity.setPromotionAmount(new BigDecimal("0.0"));
                BigDecimal originPrice = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
                BigDecimal realPrice = originPrice.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount()).subtract(itemEntity.getPromotionAmount());
                itemEntity.setRealAmount(realPrice);
                // 设置积分信息
                itemEntity.setGiftGrowth(new BigDecimal("0.0").intValue());
                itemEntity.setGiftIntegration(new BigDecimal("0.0").intValue());
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }
}