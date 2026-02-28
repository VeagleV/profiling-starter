package org.profiling;


import org.profiling.enums.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;


/***
 * Legacy profiling implementation that creates manual CGLIB enhancers for beans
 * annotated with {@link Profiling}. Kept for backward compatibility and used only
 * when legacy mode is explicitly selected.
 */
public class ProfilingHandlerBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ProfilingHandlerBeanPostProcessor.class);
    private final boolean enabled;
    private final LogType defaultLogType;

    /***
     * Creates legacy profiling post processor.
     *
     * @param enabled whether profiling is globally enabled.
     * @param defaultLogType default log rendering style for profiling output.
     */
    public ProfilingHandlerBeanPostProcessor(boolean enabled, LogType defaultLogType) {

        this.enabled = enabled;
        this.defaultLogType = defaultLogType;
    }

    /***
     * Returns bean unchanged before initialization.
     *
     * @param bean bean instance under initialization.
     * @param beanName bean name in Spring context.
     * @return same bean instance.
     * @throws BeansException when bean processing fails.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /***
     * Wraps eligible beans with legacy CGLIB proxy after initialization.
     *
     * @param bean initialized bean instance.
     * @param beanName bean name in Spring context.
     * @return proxied bean for profiling or original bean when not eligible.
     * @throws BeansException when bean processing fails.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!enabled) {
            return bean; // if turned off globally, returning same bean.
        }

        Class<?> targetClass = bean.getClass();

        //checking whether bean is annotated or not
        boolean hasAnnotatedMethods = hasAnnotatedMethods(targetClass);
        if (!targetClass.isAnnotationPresent(Profiling.class) && !hasAnnotatedMethods) return bean;

        //We can't create CGLib proxy for the final class
        if(Modifier.isFinal(targetClass.getModifiers())) {
            logger.warn("Cannot create proxy for final class: {}" + " Profiling won't work. Consider removing 'final' modifier", beanName);
            return bean;
        }

        return createCGLibProxy(bean, targetClass);

    }


    private boolean hasAnnotatedMethods(Class<?> clazz){
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Profiling.class)) {
                return true;
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return hasAnnotatedMethods(superClass);
        }

        return false;
    }


    private Object createCGLibProxy(Object target, Class<?> targetClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);

        //Setting method interceptor functionality
        enhancer.setCallback(new ProfilingMethodInterceptor(targetClass, target, defaultLogType));

        Object proxy;

        try {
            //trying to use default constructor
            Constructor<?> defaultConstructor = getDefaultConstructor(targetClass);
            if (defaultConstructor != null) {
                proxy = enhancer.create();
                return proxy;
            }
            logger.warn("No default constructor found for {}", targetClass);

            Constructor<?> constructor = Arrays.stream(targetClass.getConstructors())
                    .min(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElse(null);

            if (constructor ==  null) {
                logger.warn("No sutable constructor found for {}. returning original bean", targetClass);
                return target;
            }


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
