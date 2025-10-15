package org.upnext.authservice.configurations;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqAccountConfirmConfig {
    public static final String EMAIL_CONFIRM_QUEUE = "mail.events.queue";
    public static final String EMAIL_CONFIRM_EXCHANGE = "mail.exchange";
    public static final String EMAIL_CONFIRM_ROUTING_KEY = "mail.events";

    public TopicExchange mailConfirmExchange() {
        return new TopicExchange(EMAIL_CONFIRM_EXCHANGE);
    }

    public Queue mailConfirmQueue() {
        return new Queue(EMAIL_CONFIRM_QUEUE);
    }

    public Binding mailConfirmBinding(Queue mailConfirmQueue, TopicExchange mailConfirmExchange) {
        return BindingBuilder.bind(mailConfirmQueue).to(mailConfirmExchange).with(EMAIL_CONFIRM_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

}
