package com.qbit.framework.business.xxljob.starter.config;

import com.qbit.framework.business.xxljob.starter.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.qbit.framework.business.xxljob.starter.support.XxlJobTrigger;
import com.qbit.framework.business.xxljob.starter.actuator.XxlJobHealthIndicator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnClass(XxlJobSpringExecutor.class)
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties props, ObjectProvider<List<XxlJobExecutorCustomizer>> customizersProvider) {
        if (!StringUtils.hasText(props.getAdminAddresses())) {
            throw new IllegalStateException("xxl.job.adminAddresses 未配置");
        }
        if (!StringUtils.hasText(props.getAppname())) {
            throw new IllegalStateException("xxl.job.appname 未配置");
        }
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(props.getAdminAddresses());
        executor.setAccessToken(props.getAccessToken());
        executor.setAppname(props.getAppname());
        if (props.getIp() != null && !props.getIp().isBlank()) {
            executor.setIp(props.getIp());
        }
        if (props.getPort() != null) {
            executor.setPort(props.getPort());
        }
        executor.setLogPath(props.getLogPath());
        if (props.getLogRetentionDays() != null) {
            executor.setLogRetentionDays(props.getLogRetentionDays());
        }
        List<XxlJobExecutorCustomizer> customizers = customizersProvider.getIfAvailable();
        if (customizers != null) {
            for (XxlJobExecutorCustomizer c : customizers) {
                c.customize(executor);
            }
        }
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public XxlJobTrigger xxlJobTrigger() {
        return new XxlJobTrigger();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
    @ConditionalOnProperty(prefix = "xxl.job", name = "healthEnabled", havingValue = "true")
    @ConditionalOnMissingBean
    public XxlJobHealthIndicator xxlJobHealthIndicator(XxlJobProperties props) {
        return new XxlJobHealthIndicator(props);
    }
}
