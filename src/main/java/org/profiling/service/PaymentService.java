package org.profiling.service;

import org.profiling.Profiling;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profiling
public class PaymentService {

    public boolean processPayment(Long userId, Double amount) {
        // Симулируем медленную операцию
        simulateWork(500);  // 500ms - увидим предупреждение о медленном выполнении

        return true;
    }

    private void simulateWork(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}