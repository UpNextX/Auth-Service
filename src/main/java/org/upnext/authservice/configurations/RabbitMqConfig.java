package org.upnext.authservice.configurations;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EXCHANGE = "auth.exchange";
    public static final String ACCOUNT_CONFIRM_QUEUE = "account.confirm";
    public static final String ACCOUNT_CONFIRM_ROUTING_KEY= "account.confirm";


}
