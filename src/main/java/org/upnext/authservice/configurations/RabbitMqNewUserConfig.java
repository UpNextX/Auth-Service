package org.upnext.authservice.configurations;

import org.springframework.amqp.core.TopicExchange;

public class RabbitMqNewUserConfig {
    public static final String NEW_USER_EXCHANGE = "mail.exchange";
    public static final String NEW_USER_ROUTING_KEY = "mail.events";
    public TopicExchange mailConfirmExchange() {
        return new TopicExchange(NEW_USER_EXCHANGE);
    }
}
