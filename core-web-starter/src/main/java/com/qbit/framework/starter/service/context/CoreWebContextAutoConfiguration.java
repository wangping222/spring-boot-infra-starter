package com.qbit.framework.starter.service.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CoreWebContextAutoConfiguration {

    @Bean(name = "springApplicationContextUtils")
    @ConditionalOnMissingBean(name = "springApplicationContextUtils")
    public SpringApplicationContextUtils springApplicationContextUtils() {
        return new SpringApplicationContextUtils();
    }
}

