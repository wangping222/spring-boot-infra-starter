package com.qbit.framework.core.web.initializer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * @author Qbit Framework
 */
@AutoConfiguration
public class CoreWebInitializerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApplicationListener<ApplicationReadyEvent> applicationStartedListener() {
        return new ApplicationStartedListener();
    }

    @Bean(name = "springI18nMessageSourceInitializer")
    @ConditionalOnMissingBean(name = "springI18nMessageSourceInitializer")
    public SpringI18nMessageSourceInitializer springI18nMessageSourceInitializer() {
        return new SpringI18nMessageSourceInitializer();
    }
}
