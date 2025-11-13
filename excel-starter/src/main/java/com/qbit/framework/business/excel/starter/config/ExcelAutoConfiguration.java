package com.qbit.framework.business.excel.starter.config;

import com.qbit.framework.business.excel.starter.ExcelClient;
import com.qbit.framework.business.excel.starter.properties.ExcelProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

import java.util.Locale;

@AutoConfiguration
@EnableConfigurationProperties(ExcelProperties.class)
public class ExcelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExcelClient excelClient(ExcelProperties props, ObjectProvider<MessageSource> messageSourceProvider) {
        MessageSource ms = messageSourceProvider.getIfAvailable();
        Locale def = null;
        if (props.getDefaultLocale() != null && !props.getDefaultLocale().isBlank()) {
            def = Locale.forLanguageTag(props.getDefaultLocale());
        }
        return new ExcelClient(ms, props.isI18nEnabled(), def);
    }
}
