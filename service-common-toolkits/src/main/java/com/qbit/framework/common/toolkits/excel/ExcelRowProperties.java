package com.qbit.framework.common.toolkits.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author litao
 */
public class ExcelRowProperties {

    @Getter
    private final Row row;

    private int currentCellIndex = -1;

    public ExcelRowProperties(Row row) {
        this.row = row;
    }

    public Cell getCurrentCell() {
        return getCell(Math.max(currentCellIndex, 0));
    }

    public Cell getCell(int cellnum) {
        return row.getCell(cellnum);
    }

    private Cell getNextCell() {
        currentCellIndex++;
        Cell cell = getCurrentCell();
        if (cell == null) {
            cell = row.createCell(currentCellIndex);
        }
        return cell;
    }

    public void addCell(String value) {
        getNextCell().setCellValue(value);
    }

    public void addCell(double value) {
        getNextCell().setCellValue(value);
    }
}
