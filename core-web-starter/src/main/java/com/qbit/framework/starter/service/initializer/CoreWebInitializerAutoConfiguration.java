package com.qbit.framework.starter.service.initializer;

import com.qbit.framework.starter.service.i18n.SpringI18nMessageSourceInitializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CoreWebInitializerAutoConfiguration {

    @Bean(name = "applicationStartedListener")
    @ConditionalOnMissingBean(name = "applicationStartedListener")
    public ApplicationListener<ApplicationStartedEvent> applicationStartedListener() {
        return new ApplicationStartedListener();
    }

    @Bean(name = "springI18nMessageSourceInitializer")
    @ConditionalOnMissingBean(name = "springI18nMessageSourceInitializer")
    public SpringI18nMessageSourceInitializer springI18nMessageSourceInitializer() {
        return new SpringI18nMessageSourceInitializer();
    }
}
