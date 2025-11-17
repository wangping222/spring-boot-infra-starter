package com.qbit.framework.business.service.starter.excel;

import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.context.WriteContextImpl;
import com.alibaba.excel.enums.WriteTypeEnum;
import com.alibaba.excel.exception.ExcelGenerateException;
import com.alibaba.excel.write.ExcelBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.WriteWorkbook;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Collection;

/**
 * @author litao
 */
public class ExcelCommonBuilderImpl implements ExcelBuilder {

    private final WriteContext context;
    private ExcelWriteAddExecutor excelWriteAddExecutor;

    public ExcelCommonBuilderImpl(WriteWorkbook writeWorkbook) {
        try {
            context = new WriteContextImpl(writeWorkbook);
        } catch (RuntimeException e) {
            finishOnException();
            throw e;
        } catch (Throwable e) {
            finishOnException();
            throw new ExcelGenerateException(e);
        }
    }

    @Override
    public void addContent(Collection<?> data, WriteSheet writeSheet) {
        addContent(data, writeSheet, null, null);
    }

    @Override
    public void addContent(Collection<?> data, WriteSheet writeSheet, WriteTable writeTable) {
        addContent(data, writeSheet, null, null);
    }

    public <T> void addContent(Collection<T> data, WriteSheet writeSheet, WriteTable writeTable, ExcelRowHandleFunction<T> function) {
        try {
            context.currentSheet(writeSheet, WriteTypeEnum.ADD);
            context.currentTable(writeTable);
            if (excelWriteAddExecutor == null) {
                excelWriteAddExecutor = new ExcelWriteAddExecutor(context);
            }
            excelWriteAddExecutor.add(data, function);
        } catch (RuntimeException e) {
            finishOnException();
            throw e;
        } catch (Throwable e) {
            finishOnException();
            throw new ExcelGenerateException(e);
        }
    }

    @Override
    public void fill(Object data, FillConfig fillConfig, WriteSheet writeSheet) {
        throw new RuntimeException("Unsupported method");
    }

    private void finishOnException() {
        finish(true);
    }

    @Override
    public void finish(boolean onException) {
        if (context != null) {
            context.finish(onException);
        }
    }

    @Override
    public void merge(int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress cra = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        context.writeSheetHolder().getSheet().addMergedRegion(cra);
    }

    @Override
    public WriteContext writeContext() {
        return context;
    }
}
