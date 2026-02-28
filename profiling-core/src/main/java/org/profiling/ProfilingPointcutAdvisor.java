package org.profiling;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

/***
 * Advisor that applies profiling advice to classes or methods annotated with {@link Profiling}.
 */
public class ProfilingPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {

    /***
     * Creates advisor that routes matched invocations to profiling advice.
     *
     * @param advice method advice that performs profiling.
     */
    public ProfilingPointcutAdvisor(Advice advice) {
        setAdvice(advice);
        setClassFilter(new ProfilingClassFilter());
    }

    /***
     * Matches methods that should be profiled.
     *
     * @param method method selected by Spring AOP.
     * @param targetClass actual bean class where the method is invoked.
     * @return {@code true} when class-level or method-level {@link Profiling} annotation is present.
     */
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

    /***
     * Resolves method on the target class hierarchy using the same signature.
     *
     * @param type runtime type that owns the method.
     * @param method source method descriptor from proxy invocation.
     * @return concrete method from class hierarchy or {@code null} when not found.
     */
    private Method findTargetMethod(Class<?> type, Method method) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Method declaredMethod = current.getDeclaredMethod(method.getName(), method.getParameterTypes());
                return declaredMethod;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static final class ProfilingClassFilter implements ClassFilter {

        /***
         * Excludes infrastructure classes from profiling advisor application.
         *
         * @param clazz class to evaluate.
         * @return {@code true} when class is eligible for profiling advice.
         */
        @Override
        public boolean matches(Class<?> clazz) {
            return !AopInfrastructureBean.class.isAssignableFrom(clazz)
                    && !Advice.class.isAssignableFrom(clazz)
                    && !BeanPostProcessor.class.isAssignableFrom(clazz);
        }
    }
}
