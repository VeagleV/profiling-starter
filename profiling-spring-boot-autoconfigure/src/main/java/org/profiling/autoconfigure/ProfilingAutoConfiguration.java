package org.profiling.autoconfigure;

import org.profiling.ProfilingHandlerBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ProfilingHandlerBeanPostProcessor.class)
@EnableConfigurationProperties(ProfilingProperties.class)
@ConditionalOnProperty(prefix = "profiling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProfilingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProfilingHandlerBeanPostProcessor profilingHandlerBeanPostProcessor(ProfilingProperties properties) {
        return new ProfilingHandlerBeanPostProcessor(
                properties.isEnabled(),
                properties.getLogType()
        );
    }
}