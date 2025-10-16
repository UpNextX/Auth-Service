package org.upnext.authservice.configurations;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Configuration;
// in case of separation between auth and users this should be in user service
@Configuration
public class RabbitMqNotificationConfig {
    public static final String NOTIFICATION_EXCHANGE = "notifications.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notifications.events";

    public TopicExchange topicExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
}
