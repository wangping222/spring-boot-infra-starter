package com.qbit.framework.business.service.starter.excel;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;

/**
 * @author litao
 */
public class ExcelCommonWriterBuilder extends ExcelWriterBuilder {

    public ExcelCommonWriterBuilder() {
        super();
        this.excelType(ExcelTypeEnum.XLSX);
    }

    public ExcelCommonWriter build2() {
        return new ExcelCommonWriter(parameter());
    }
}
