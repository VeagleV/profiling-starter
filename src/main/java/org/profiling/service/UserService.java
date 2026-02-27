package org.profiling.service;

import org.profiling.model.User;
import org.profiling.Profiling;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Profiling  // Все методы класса будут профилироваться
public class UserService {

    public User findUserById(Long id) {
        simulateWork(50);

        User user = new User();
        user.setId(id);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setAge(30);

        return user;
    }

    public User createUser(String name, String email, Integer age) {
        simulateWork(100);

        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);

        return user;
    }

    public List<User> findUsersByIds(List<Long> ids) {
        simulateWork(150);

        List<User> users = new ArrayList<>();
        for (Long id : ids) {
            User user = new User();
            user.setId(id);
            user.setName("User " + id);
            users.add(user);
        }

        return users;
    }

    public String getUserStats(Long userId) {
        // Этот метод вызывает приватный метод
        User user = findUserById(userId);
        int loginCount = calculateLoginCount(userId);

        return String.format("User %s has %d logins", user.getName(), loginCount);
    }

    private int calculateLoginCount(Long userId) {
        // Private метод - тоже будет профилирован!
        simulateWork(30);
        return (int) (userId % 100);
    }

    public void deleteUser(Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        simulateWork(50);
    }

    @Profiling(logParams = false)  // Не логируем пароли!
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        simulateWork(75);
        // Пароли не будут залогированы
    }

    public User findUserByEmail(String email) {
        simulateWork(60);
        // Возвращаем null для тестирования
        return null;
    }

    public User updateUser(User user) {
        simulateWork(80);
        return user;
    }

    protected void simulateWork(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}