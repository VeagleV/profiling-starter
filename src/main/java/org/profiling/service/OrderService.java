package org.profiling.service;

import org.profiling.Profiling;
import org.profiling.model.Order;
import org.profiling.model.User;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profiling
public class OrderService {

    private final UserService userService;
    private final PaymentService paymentService;

    public OrderService(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    public Order createOrder(Long userId, String productName, int quantity) {
        // Этот метод вызывает другие профилируемые методы
        // Вы увидите вложенные вызовы в логах

        User user = userService.findUserById(userId);

        simulateWork(100);

        Order order = new Order();
        order.setId(System.currentTimeMillis());
        order.setUserId(userId);
        order.setProductName(productName);
        order.setQuantity(quantity);
        order.setTotalPrice(49.99 * quantity);

        // Обрабатываем платеж
        paymentService.processPayment(userId, order.getTotalPrice());

        return order;
    }

    private void simulateWork(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}