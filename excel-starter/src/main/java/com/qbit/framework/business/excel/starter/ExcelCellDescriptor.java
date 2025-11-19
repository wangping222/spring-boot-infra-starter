package com.qbit.framework.business.excel.starter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExcelCellDescriptor<T> {
    private final String header;
    private final java.util.function.Function<T, Object> valueExtractor;


    public static <T> ExcelCellDescriptor<T> of(String header, java.util.function.Function<T, Object> valueExtractor) {
        return new ExcelCellDescriptor<>(header, valueExtractor);
    }

}
