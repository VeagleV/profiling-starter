package org.profiling.autoconfigure;

import org.junit.jupiter.api.Test;
import org.profiling.Profiling;
import org.profiling.ProfilingHandlerBeanPostProcessor;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ProfilingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ProfilingAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void aopModeIsEnabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(InfrastructureAdvisorAutoProxyCreator.class);
            assertThat(context).doesNotHaveBean(ProfilingHandlerBeanPostProcessor.class);
            TestService bean = context.getBean(TestService.class);
            assertThat(AopProxyUtils.ultimateTargetClass(bean)).isEqualTo(TestService.class);
            assertThat(bean.call()).isEqualTo("ok");
        });
    }

    @Test
    void legacyModeEnablesBeanPostProcessor() {
        contextRunner
                .withPropertyValues("profiling.mode=legacy")
                .run(context -> {
                    assertThat(context).hasSingleBean(ProfilingHandlerBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(InfrastructureAdvisorAutoProxyCreator.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {
        @Bean
        TestService testService() {
            return new TestService();
        }
    }

    @Profiling
    public static class TestService {
        public String call() {
            return "ok";
        }
    }
}
