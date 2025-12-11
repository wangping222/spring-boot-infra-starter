package com.qbit.framework.core.web.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CoreWebContextAutoConfiguration {

    @Bean(name = "applicationContextUtils")
    @ConditionalOnMissingBean(name = "applicationContextUtils")
    public ApplicationContextUtils springApplicationContextUtils() {
        return new ApplicationContextUtils();
    }
}

