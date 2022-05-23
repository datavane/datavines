package io.datavines.notification.core;

import org.springframework.boot.SpringApplication;

public class NotificationService {

    public static void main(String[] args) {
        Thread.currentThread().setName(NotificationService.class.getSimpleName());
        SpringApplication.run(NotificationService.class);
    }

}
