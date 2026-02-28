package org.profiling.autoconfigure;

import org.profiling.ProfilingAopMethodInterceptor;
import org.profiling.ProfilingHandlerBeanPostProcessor;
import org.profiling.ProfilingPointcutAdvisor;
import org.springframework.aop.Advisor;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

/***
 * Auto-configuration that wires profiling either through Spring AOP infrastructure (default)
 * or the legacy enhancer-based bean post processor for compatibility.
 */
@AutoConfiguration
@ConditionalOnClass(ProfilingHandlerBeanPostProcessor.class)
@EnableConfigurationProperties(ProfilingProperties.class)
@ConditionalOnProperty(prefix = "profiling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProfilingAutoConfiguration {

    /***
     * Registers infrastructure auto-proxy creator used to apply profiling advisor.
     *
     * @return infrastructure auto-proxy creator configured for class-based proxies.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = "profiling", name = "mode", havingValue = "AOP", matchIfMissing = true)
    @ConditionalOnMissingBean(name = AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME)
    public static InfrastructureAdvisorAutoProxyCreator profilingAutoProxyCreator() {
        InfrastructureAdvisorAutoProxyCreator creator = new InfrastructureAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    /***
     * Registers advisor that applies profiling to {@link org.profiling.Profiling}-annotated targets.
     *
     * @param properties starter properties used to configure advice behavior.
     * @return profiling advisor bean.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = "profiling", name = "mode", havingValue = "AOP", matchIfMissing = true)
    @ConditionalOnMissingBean(ProfilingPointcutAdvisor.class)
    public Advisor profilingAdvisor(ProfilingProperties properties) {
        return new ProfilingPointcutAdvisor(new ProfilingAopMethodInterceptor(properties.getLogType()));
    }

    /***
     * Creates deprecated enhancer-based bean post processor for compatibility mode.
     *
     * @param properties starter properties used by legacy profiling implementation.
     * @return legacy profiling bean post processor.
     * @deprecated Legacy mode fallback. Prefer {@link ProfilingMode#AOP}.
     */
    @Deprecated
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "profiling", name = "mode", havingValue = "LEGACY")
    public static ProfilingHandlerBeanPostProcessor profilingHandlerBeanPostProcessor(ProfilingProperties properties) {
        return new ProfilingHandlerBeanPostProcessor(
                properties.isEnabled(),
                properties.getLogType()
        );
    }
}
