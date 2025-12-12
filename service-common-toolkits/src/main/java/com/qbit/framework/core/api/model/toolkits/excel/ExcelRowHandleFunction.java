package com.qbit.framework.core.api.model.toolkits.excel;

/**
 * @author Qbit Framework
 */
@FunctionalInterface
public interface ExcelRowHandleFunction<T> {
    void handle(ExcelRowProperties row, T data);
}
