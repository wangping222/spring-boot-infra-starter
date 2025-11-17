package com.qbit.framework.business.service.starter.excel;

import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.WriteWorkbook;

import java.util.Collection;

/**
 * @author litao
 */
public class ExcelCommonWriter {

    private final ExcelCommonBuilderImpl excelBuilder;

    public ExcelCommonWriter(WriteWorkbook writeWorkbook) {
        excelBuilder = new ExcelCommonBuilderImpl(writeWorkbook);
    }

    /**
     * Write data to a sheet
     *
     * @param data       Data to be written
     * @param writeSheet Write to this sheet
     * @return this current writer
     */
    public <T> ExcelCommonWriter write(Collection<T> data, WriteSheet writeSheet, ExcelRowHandleFunction<T> function) {
        return write(data, writeSheet, null, function);
    }

    /**
     * Write value to a sheet
     *
     * @param data       Data to be written
     * @param writeSheet Write to this sheet
     * @param writeTable Write to this table
     * @return this
     */
    public <T> ExcelCommonWriter write(Collection<T> data, WriteSheet writeSheet, WriteTable writeTable, ExcelRowHandleFunction<T> function) {
        excelBuilder.addContent(data, writeSheet, writeTable, function);
        return this;
    }

    public void finish() {
        excelBuilder.finish(false);
    }

    public WriteContext writeContext() {
        return excelBuilder.writeContext();
    }
}
