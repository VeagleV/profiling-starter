package org.profiling.controller;

import org.profiling.model.User;
import org.profiling.service.OrderService;
import org.profiling.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {

    private final UserService userService;
    private final OrderService orderService;

    public TestController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user.getName(), user.getEmail(), user.getAge());
    }

    @PostMapping("/orders")
    public Object createOrder(@RequestParam Long userId,
                              @RequestParam String product,
                              @RequestParam int quantity) {
        return orderService.createOrder(userId, product, quantity);
    }

    @GetMapping("/users/search")
    public List<User> searchUsers(@RequestParam List<Long> ids) {
        return userService.findUsersByIds(ids);
    }
}