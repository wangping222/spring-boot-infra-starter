package com.qbit.framework.business.excel.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "excel")
@Data
public class ExcelProperties {
    private String defaultSheetName = "Sheet1";
    private boolean i18nEnabled = true;
    private String defaultLocale;
}
