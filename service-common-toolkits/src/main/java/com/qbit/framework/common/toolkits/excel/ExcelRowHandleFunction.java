package com.qbit.framework.common.toolkits.excel;

@FunctionalInterface
public interface ExcelRowHandleFunction<T> {
    void handle(ExcelRowProperties row, T data);
}
