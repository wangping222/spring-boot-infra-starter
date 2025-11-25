package com.qbit.framework.starter.service.excel;

@FunctionalInterface
public interface ExcelRowHandleFunction<T> {
    void handle(ExcelRowProperties row, T data);
}
