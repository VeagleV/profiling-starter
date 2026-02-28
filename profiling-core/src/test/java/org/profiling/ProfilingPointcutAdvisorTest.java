package org.profiling;

import org.junit.jupiter.api.Test;
import org.profiling.enums.LogType;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfilingPointcutAdvisorTest {

    private final ProfilingPointcutAdvisor advisor =
            new ProfilingPointcutAdvisor(new ProfilingAopMethodInterceptor(LogType.SIMPLE));

    @Test
    void matchesClassLevelAnnotation() throws Exception {
        Method method = ClassAnnotatedService.class.getDeclaredMethod("work");
        assertTrue(advisor.matches(method, ClassAnnotatedService.class));
    }

    @Test
    void matchesMethodLevelAnnotation() throws Exception {
        Method method = MethodAnnotatedService.class.getDeclaredMethod("work");
        assertTrue(advisor.matches(method, MethodAnnotatedService.class));
    }

    @Test
    void doesNotMatchWithoutProfilingAnnotation() throws Exception {
        Method method = PlainService.class.getDeclaredMethod("work");
        assertFalse(advisor.matches(method, PlainService.class));
    }

    @Profiling
    static class ClassAnnotatedService {
        void work() {
        }
    }

    static class MethodAnnotatedService {
        @Profiling
        void work() {
        }
    }

    static class PlainService {
        void work() {
        }
    }
}
