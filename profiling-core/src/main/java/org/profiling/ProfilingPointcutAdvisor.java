package org.profiling;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

/**
 * Advisor that applies profiling advice to classes or methods annotated with {@link Profiling}.
 */
public class ProfilingPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {

    public ProfilingPointcutAdvisor(Advice advice) {
        setAdvice(advice);
        setClassFilter(new ProfilingClassFilter());
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (targetClass == null) {
            return false;
        }

        if (targetClass.isAnnotationPresent(Profiling.class)) {
            return true;
        }

        Method targetMethod = findTargetMethod(targetClass, method);
        return targetMethod != null && targetMethod.isAnnotationPresent(Profiling.class);
    }

    private Method findTargetMethod(Class<?> type, Method method) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Method declaredMethod = current.getDeclaredMethod(method.getName(), method.getParameterTypes());
                declaredMethod.setAccessible(true);
                return declaredMethod;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static final class ProfilingClassFilter implements ClassFilter {

        @Override
        public boolean matches(Class<?> clazz) {
            return !AopInfrastructureBean.class.isAssignableFrom(clazz)
                    && !Advice.class.isAssignableFrom(clazz)
                    && !BeanPostProcessor.class.isAssignableFrom(clazz);
        }
    }
}
