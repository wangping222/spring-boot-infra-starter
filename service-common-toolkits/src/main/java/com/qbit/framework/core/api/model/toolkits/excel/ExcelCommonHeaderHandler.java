package com.qbit.framework.core.api.model.toolkits.excel;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * 通用excel头
 *
 * @author litao
 */
public class ExcelCommonHeaderHandler implements SheetWriteHandler {

    @Getter
    private final String[] headers;

    public ExcelCommonHeaderHandler(String[] headers) {
        this.headers = headers;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            Cell cell = row.createCell(i);
            // 设置单元格内容
            cell.setCellValue(header);
        }
        writeSheetHolder.setNeedHead(false);
        writeSheetHolder.setUseDefaultStyle(false);
        writeSheetHolder.setRelativeHeadRowIndex(1);
    }
}
