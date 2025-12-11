package com.qbit.framework.core.api.model.toolkits.excel;

@FunctionalInterface
public interface ExcelRowHandleFunction<T> {
    void handle(ExcelRowProperties row, T data);
}
