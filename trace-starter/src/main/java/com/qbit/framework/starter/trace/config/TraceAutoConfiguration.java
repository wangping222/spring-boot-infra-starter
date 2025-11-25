package com.qbit.framework.starter.trace.config;

import com.qbit.framework.starter.trace.properties.TraceProperties;
import com.qbit.framework.starter.trace.web.TraceInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
@EnableConfigurationProperties(TraceProperties.class)
@ConditionalOnProperty(prefix = "trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "trace", name = "web-enabled", havingValue = "true", matchIfMissing = true)
    public TraceInterceptor traceInterceptor() {
        return new TraceInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "trace", name = "web-enabled", havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer traceWebMvcConfigurer(TraceInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor).addPathPatterns("/**");
            }
        };
    }
}
