package com.qbit.framework.core.api.model.toolkits.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@AllArgsConstructor
@Getter
public class ExcelCellDescriptor<T> {
    private final String header;
    private final Function<T, Object> valueExtractor;


    public static <T> ExcelCellDescriptor<T> of(String header, Function<T, Object> valueExtractor) {
        return new ExcelCellDescriptor<>(header, valueExtractor);
    }
}
