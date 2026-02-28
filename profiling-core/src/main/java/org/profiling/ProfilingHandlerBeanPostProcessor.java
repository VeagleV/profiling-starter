package org.profiling;


import org.profiling.enums.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;



/**
 * Legacy profiling implementation that creates manual CGLIB enhancers for beans
 * annotated with {@link Profiling}. Kept for backward compatibility and used only
 * when legacy mode is explicitly selected.
 */
public class ProfilingHandlerBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ProfilingHandlerBeanPostProcessor.class);
    private final boolean enabled;
    private final LogType defaultLogType;

    public ProfilingHandlerBeanPostProcessor(boolean enabled, LogType defaultLogType) {

        this.enabled = enabled;
        this.defaultLogType = defaultLogType;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!enabled) {
            return bean; // глобально выключено
        }

        Class<?> targetClass = bean.getClass();


        boolean hasAnnotedMethods = hasAnnotedMethods(targetClass);

        if (!targetClass.isAnnotationPresent(Profiling.class) && !hasAnnotedMethods) return bean;

        if(Modifier.isFinal(targetClass.getModifiers())) {
            logger.warn("Cannot create proxy for final class: {}" + " Profiling won't work. Consider removing 'final' modifier", beanName);
            return bean;
        }

        return createCGLibProxy(bean, targetClass);

    }


    private boolean hasAnnotedMethods(Class<?> clazz){
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Profiling.class)) {
                return true;
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return hasAnnotedMethods(superClass);
        }

        return false;
    }


    private Object createCGLibProxy(Object target, Class<?> targetClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new ProfilingMethodInterceptor(targetClass, target, defaultLogType));

        Object proxy;

        try {
            //Пытаемся использовать дефолтный конструктор
            Constructor<?> defaultConstructor = getDefaultConstructor(targetClass);
            if (defaultConstructor != null) {
                proxy = enhancer.create();
                return proxy;
            }

            logger.warn("No default constructor found for {}", targetClass);
            Constructor<?>[] constructors = targetClass.getDeclaredConstructors();
            if (constructors.length == 0) {
                logger.warn( "No sutable constructor found for " + targetClass + ". returning original bean");
                return target;
            }

            Constructor<?> constructor = constructors[0];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            proxy = enhancer.create(parameterTypes, parameters);
            return proxy;

        } catch (Exception e) {
            logger.warn("Cannot create CGLib proxy for class(error occurred while copying fields to proxy class): {}" + " Profiling won't work, returning original bean", targetClass.getName(), e);
            return target;
        }
    }


    private Constructor<?> getDefaultConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
