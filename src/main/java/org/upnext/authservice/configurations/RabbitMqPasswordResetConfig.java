package org.upnext.authservice.configurations;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqPasswordResetConfig {
    public static final String FORGET_PASS_QUEUE = "forget.pass.events.queue";
    public static final String FORGET_PASS_EXCHANGE = "forget.pass.exchange";
    public static final String FORGET_PASS_ROUTING_KEY = "forget.pass.events";

    public TopicExchange forgetPassExchange() {
        return new TopicExchange(FORGET_PASS_EXCHANGE);
    }

    public Queue forgetPassEventsQueue() {
        return new Queue(FORGET_PASS_QUEUE);
    }

    public Binding forgetPassBinding() {
        return BindingBuilder.bind(forgetPassEventsQueue()).to(forgetPassExchange())
                .with(FORGET_PASS_ROUTING_KEY);
    }
}
