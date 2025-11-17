package com.qbit.framework.business.excel.starter;

public class ExcelCellDescriptor<T> {
    private final String header;
    private final java.util.function.Function<T, Object> valueExtractor;

    public ExcelCellDescriptor(String header, java.util.function.Function<T, Object> valueExtractor) {
        this.header = header;
        this.valueExtractor = valueExtractor;
    }

    public static <T> ExcelCellDescriptor<T> of(String header, java.util.function.Function<T, Object> valueExtractor) {
        return new ExcelCellDescriptor<>(header, valueExtractor);
    }

    public String getHeader() { return header; }
    public java.util.function.Function<T, Object> getValueExtractor() { return valueExtractor; }
}
