package org.upnext.authservice.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.upnext.authservice.models.User;
import org.upnext.authservice.repositories.UserRepository;
import org.upnext.authservice.services.UserService;
import org.upnext.sharedlibrary.Dtos.UserDto;
import org.upnext.sharedlibrary.Events.NotificationEvent;
import org.upnext.sharedlibrary.Events.ProductEvent;

import java.util.List;

import static org.upnext.authservice.configurations.RabbitMqNotificationConfig.NOTIFICATION_EXCHANGE;
import static org.upnext.authservice.configurations.RabbitMqNotificationConfig.NOTIFICATION_ROUTING_KEY;
import static org.upnext.authservice.configurations.RabbitMqProductsConfig.PRODUCT_QUEUE;
// in case of separation between auth and users this should be in user service
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);

    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = PRODUCT_QUEUE)
    public void handleNewProductAdded(ProductEvent productEvent) {
        logger.info("Product event received : {}", productEvent);
        List<UserDto> userList = userService.loadAllUsers();
        NotificationEvent event = new NotificationEvent();
        event.setId(productEvent.getId());
        event.setName(productEvent.getName());
        event.setBrand(productEvent.getBrand());
        event.setDescription(productEvent.getDescription());
        event.setPrice(productEvent.getPrice());
        event.setCategory(productEvent.getCategory());
        event.setUrl(productEvent.getUrl());
        userList.forEach(user -> {
            event.setUser_email(user.getEmail());
            event.setUser_name(user.getName());
            sendEmail(event);
        });
    }

    private void sendEmail(NotificationEvent notificationEvent) {
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, notificationEvent);
    }
}
