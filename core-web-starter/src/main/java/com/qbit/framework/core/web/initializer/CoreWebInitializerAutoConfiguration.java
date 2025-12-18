package com.qbit.framework.core.web.initializer;

import com.qbit.framework.core.web.advice.GlobalExceptionAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author Qbit Framework
 */
@AutoConfiguration
public class CoreWebInitializerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SystemInitializationLifecycle applicationStartedListener() {
        return new SystemInitializationLifecycle();
    }

    @Bean(name = "springI18nMessageSourceInitializer")
    @ConditionalOnMissingBean(name = "springI18nMessageSourceInitializer")
    public SpringI18nMessageSourceInitializer springI18nMessageSourceInitializer() {
        return new SpringI18nMessageSourceInitializer();
    }

    /**
     * 全局异常处理 Advice
     * <p>通过配置 framework.web.exception-advice.enabled 来控制是否启用
     * <p>默认为 true，即默认启用全局异常处理
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionAdvice.class)
    @ConditionalOnProperty(prefix = "framework.web.exception-advice", name = "enabled", havingValue = "true", matchIfMissing = true)
    public GlobalExceptionAdvice globalExceptionAdvice() {
        return new GlobalExceptionAdvice();
    }
}
