package org.upnext.authservice.configurations;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqPasswordResetConfig {
    public static final String FORGET_PASS_QUEUE = "forget.pass.events.queue";
    public static final String FORGET_PASS_EXCHANGE = "forget.pass.exchange";
    public static final String FORGET_PASS_ROUTING_KEY = "forget.pass.events";

    public TopicExchange forgetPassExchange() {
        return new TopicExchange(FORGET_PASS_EXCHANGE);
    }


}
