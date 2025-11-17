package com.qbit.framework.business.service.starter.excel;

@FunctionalInterface
public interface ExcelRowHandleFunction<T> {
    void handle(ExcelRowProperties row, T data);
}
