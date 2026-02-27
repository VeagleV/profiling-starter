package org.profiling;

import org.profiling.autoconfigure.ProfilingProperties;
import org.profiling.model.User;
import org.profiling.service.OrderService;
import org.profiling.service.PaymentService;
import org.profiling.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(ProfilingProperties.class)
public class ProfilingApplication {

    private static final Logger logger = LoggerFactory.getLogger(ProfilingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProfilingApplication.class, args);
    }

    @Bean
    public CommandLineRunner testRunner(UserService userService,
                                        OrderService orderService,
                                        PaymentService paymentService) {
        return args -> {
            logger.info("\n\n" + "=".repeat(100));
            logger.info("🚀 Starting Profiling Library Tests...");
            logger.info("=".repeat(100) + "\n");

            // Тест 1: Простой метод
            logger.info("\n📌 TEST 1: Simple method call");
            userService.findUserById(123L);

            Thread.sleep(500);

            // Тест 2: Метод с несколькими параметрами
            logger.info("\n📌 TEST 2: Method with multiple parameters");
            userService.createUser("John Doe", "john@example.com", 25);

            Thread.sleep(500);

            // Тест 3: Метод с коллекциями
            logger.info("\n📌 TEST 3: Method with collections");
            userService.findUsersByIds(java.util.Arrays.asList(1L, 2L, 3L, 4L, 5L));

            Thread.sleep(500);

            // Тест 4: Private метод (вызывается через public)
            logger.info("\n📌 TEST 4: Private method (called internally)");
            userService.getUserStats(123L);

            Thread.sleep(500);

            // Тест 5: Вложенные вызовы
            logger.info("\n📌 TEST 5: Nested calls");
            orderService.createOrder(123L, "Product XYZ", 2);

            Thread.sleep(500);

            // Тест 6: Метод с исключением
            logger.info("\n📌 TEST 6: Method throwing exception");
            try {
                userService.deleteUser(-1L);
            } catch (Exception e) {
                logger.info("Exception caught: {}", e.getMessage());
            }

            Thread.sleep(500);

            // Тест 7: Медленный метод
            logger.info("\n📌 TEST 7: Slow method");
            paymentService.processPayment(123L, 99.99);

            Thread.sleep(500);

            // Тест 8: Метод без логирования параметров
            logger.info("\n📌 TEST 8: Method without parameter logging");
            userService.changePassword(123L, "oldPassword", "newPassword");

            Thread.sleep(500);

            // Тест 9: Метод возвращающий null
            logger.info("\n📌 TEST 9: Method returning null");
            userService.findUserByEmail("nonexistent@example.com");

            Thread.sleep(500);

            // Тест 10: Метод с большим объектом
            logger.info("\n📌 TEST 10: Method with large object");
            userService.updateUser(createLargeUser());

            logger.info("\n\n" + "=".repeat(100));
            logger.info("✅ All tests completed!");
            logger.info("=".repeat(100) + "\n");
        };
    }

    private User createLargeUser() {
        User user = new User();
        user.setId(999L);
        user.setName("Very Long Name With Many Characters To Test Truncation Feature");
        user.setEmail("test@example.com");
        user.setAge(30);
        return user;
    }

}
