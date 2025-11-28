package com.qbit.framework.starter.excel;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author litao
 * @description ExcelExportBO
 * @date 2023/7/24 9:39
 */
@Data
public class ExcelExportBO<T> implements Serializable {
    /**
     * serial version UID
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * sheet name
     */
    private String sheetName;

    /**
     * data
     */
    private transient Collection<T> data;

    /**
     * class header(优先级高于自定义表头)
     */
    private Class<?> clazz;

    /**
     * 自定义表头
     */
    private List<List<String>> headers;

    /**
     * 是否使用自定义头部信息(默认值: true)
     */
    private Boolean useDefaultHeaderInfo;

    /**
     * 自定义头部信息
     */
    private String[][] headerInfo;

    /**
     * i18n表头
     */
    private Boolean i18nHeader;

    public void setHeaders(String[] headers) {
        if (headers == null) {
            this.headers = null;
            return;
        }
        this.headers = new ArrayList<>(headers.length);
        for (String header : headers) {
            this.headers.add(List.of(header));
        }
    }

    public void setHeaders(List<List<String>> headers) {
        this.headers = headers;
    }
}
