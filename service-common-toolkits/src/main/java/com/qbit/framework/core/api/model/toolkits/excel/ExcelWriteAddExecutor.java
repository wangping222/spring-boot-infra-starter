package com.qbit.framework.core.api.model.toolkits.excel;

import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.util.WorkBookUtil;
import com.alibaba.excel.util.WriteHandlerUtils;
import com.alibaba.excel.write.executor.AbstractExcelWriteExecutor;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author litao
 */
public class ExcelWriteAddExecutor extends AbstractExcelWriteExecutor {

    public ExcelWriteAddExecutor(WriteContext writeContext) {
        super(writeContext);
    }

    public <T> void add(Collection<T> data, ExcelRowHandleFunction<T> function) {
        if (CollectionUtils.isEmpty(data)) {
            data = new ArrayList<>();
        }
        WriteSheetHolder writeSheetHolder = writeContext.writeSheetHolder();
        int newRowIndex = writeSheetHolder.getNewRowIndexAndStartDoWrite();
        if (writeSheetHolder.isNew() && !writeSheetHolder.getExcelWriteHeadProperty().hasHead()) {
            newRowIndex += writeContext.currentWriteHolder().relativeHeadRowIndex();
        }
        int relativeRowIndex = 0;
        for (T oneRowData : data) {
            int lastRowIndex = relativeRowIndex + newRowIndex;
            addOneRowOfDataToExcel(oneRowData, lastRowIndex, relativeRowIndex, function);
            relativeRowIndex++;
        }
    }

    private <T> void addOneRowOfDataToExcel(T oneRowData, int rowIndex, int relativeRowIndex,
            ExcelRowHandleFunction<T> function) {
        if (oneRowData == null) {
            return;
        }
        RowWriteHandlerContext rowWriteHandlerContext = WriteHandlerUtils.createRowWriteHandlerContext(writeContext,
                rowIndex, relativeRowIndex, Boolean.FALSE);
        WriteHandlerUtils.beforeRowCreate(rowWriteHandlerContext);

        Row row = WorkBookUtil.createRow(writeContext.writeSheetHolder().getSheet(), rowIndex);
        rowWriteHandlerContext.setRow(row);

        WriteHandlerUtils.afterRowCreate(rowWriteHandlerContext);

        ExcelRowProperties properties = new ExcelRowProperties(row);

        function.handle(properties, oneRowData);

        WriteHandlerUtils.afterRowDispose(rowWriteHandlerContext);
    }

}