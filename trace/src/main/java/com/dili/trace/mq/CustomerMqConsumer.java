package com.dili.trace.mq;

import com.alibaba.fastjson.JSON;
import com.dili.customer.sdk.constants.MqConstant;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.trace.service.SyncUserInfoService;
import com.dili.trace.service.UserInfoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * customer mq consumer
 */
@Component
public class CustomerMqConsumer {
    private static final Logger logger = LoggerFactory.getLogger(CustomerMqConsumer.class);
    @Autowired
    UserInfoService userInfoService;

    /**
     * 新增customer
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "trace-add-customer", durable = "true"),
            exchange = @Exchange(name = MqConstant.CUSTOMER_ADD_MQ_FANOUT_EXCHANGE,
                    durable = "true",
                    type = ExchangeTypes.FANOUT,
                    ignoreDeclarationExceptions = "true")
    )
    )

    @RabbitHandler
    public void addCustomer(Message message, Channel channel) throws Exception {
        //	1. 收到消息以后进行业务端消费处理
        logger.info("-----------------------");
        logger.info("消费消息:" + message.getPayload());

        //  2. 处理成功之后 获取deliveryTag 并进行手工的ACK操作, 因为我们配置文件里配置的是 手工签收
        //	spring.rabbitmq.listener.simple.acknowledge-mode=manual
        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);

        try{
            channel.basicAck(deliveryTag, false);
            this.convertJson(message).ifPresent(extendDto -> {
                this.userInfoService.updateUserInfoByCustomerExtendDto(extendDto);
            });
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            channel.basicNack(deliveryTag, false,true);
        }
    }

    /**
     * 更新customer
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "trace-update-customer", durable = "true"),
            exchange = @Exchange(name = MqConstant.CUSTOMER_MQ_FANOUT_EXCHANGE,
                    durable = "true",
                    type = ExchangeTypes.FANOUT,
                    ignoreDeclarationExceptions = "true")
    )
    )
    @RabbitHandler
    public void updateCustomer(Message message, Channel channel) throws Exception {
        //	1. 收到消息以后进行业务端消费处理
        logger.info("-----------------------");
        logger.info("消费消息:" + message.getPayload());
        this.convertJson(message).ifPresent(extendDto -> {
            this.userInfoService.updateUserInfoByCustomerExtendDto(extendDto);
        });
        //  2. 处理成功之后 获取deliveryTag 并进行手工的ACK操作, 因为我们配置文件里配置的是 手工签收
        //	spring.rabbitmq.listener.simple.acknowledge-mode=manual
        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 消息格式转换
     * @param message
     * @return
     */
    private Optional<CustomerExtendDto> convertJson(Message message) {
        try {
            String msg = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            CustomerExtendDto extendDto = JSON.parseObject(msg, CustomerExtendDto.class);
            return Optional.of(extendDto);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();

    }

}
