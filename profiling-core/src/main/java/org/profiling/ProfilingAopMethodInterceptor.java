package org.profiling;

import org.aopalliance.intercept.MethodInvocation;
import org.profiling.enums.LogType;

/**
 * Spring AOP adapter that reuses {@link ProfilingMethodInterceptor} logic to preserve
 * existing profiling behavior while switching proxy infrastructure.
 */
public class ProfilingAopMethodInterceptor implements org.aopalliance.intercept.MethodInterceptor {

    private final LogType defaultLogType;

    public ProfilingAopMethodInterceptor(LogType defaultLogType) {
        this.defaultLogType = defaultLogType;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object target = invocation.getThis();
        Class<?> targetClass = target != null ? target.getClass() : invocation.getMethod().getDeclaringClass();
        ProfilingMethodInterceptor delegate = new ProfilingMethodInterceptor(targetClass, target, defaultLogType);
        return delegate.intercept(target, invocation.getMethod(), invocation.getArguments(), null);
    }
}
