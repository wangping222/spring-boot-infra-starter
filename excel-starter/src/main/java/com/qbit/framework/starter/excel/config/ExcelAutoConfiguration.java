package com.qbit.framework.starter.excel.config;

import com.qbit.framework.starter.excel.ExcelClient;
import com.qbit.framework.starter.excel.properties.ExcelProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@AutoConfiguration
@EnableConfigurationProperties(ExcelProperties.class)
public class ExcelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExcelClient excelClient(ExcelProperties props) {
        return new ExcelClient(props);
    }
}
