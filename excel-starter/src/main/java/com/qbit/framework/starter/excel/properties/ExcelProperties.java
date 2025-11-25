package com.qbit.framework.starter.excel.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "framework.excel")
@Data
public class ExcelProperties {
    private String defaultSheetName = "Sheet1";
}
