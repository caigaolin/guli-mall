package com.muke.gulimall.wms.constant;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/4/5 16:33
 */
public interface MqConstant {

    String WARE_EXCHANGE = "ware-event-exchange";

    String DELAY_QUEUE = "ware.delay.queue";

    String RELEASE_QUEUE = "ware.release.stock.queue";

    String DELAY_QUEUE_ROUTING_KEY = "ware.create";

    String RELEASE_QUEUE_ROUTING_KEY = "ware.release.#";

    Integer DELAY_QUEUE_TTL = 600000;
}
