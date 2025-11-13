package com.qbit.framework.business.excel.starter;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.annotation.ExcelProperty;
import org.springframework.context.MessageSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ExcelClient {
    private final MessageSource messageSource;
    private final boolean i18nEnabled;
    private final Locale defaultLocale;

    public ExcelClient(MessageSource messageSource, boolean i18nEnabled, Locale defaultLocale) {
        this.messageSource = messageSource;
        this.i18nEnabled = i18nEnabled;
        this.defaultLocale = defaultLocale;
    }

    public <T> void write(String fileName, Class<T> clazz, List<T> data, String sheetName) {
        writeInternal(fileName, clazz, data, sheetName, null, null);
    }

    public <T> void write(String fileName, List<T> data, String sheetName) {
        Class<T> clazz = inferClass(data);
        FastExcel.write(fileName, clazz).sheet(localizeSheet(sheetName, null)).doWrite(data);
    }

    public <T> void write(String fileName, Class<T> clazz, List<T> data, String sheetName, java.util.function.UnaryOperator<T> converter) {
        writeInternal(fileName, clazz, data, sheetName, null, converter);
    }

    public <T> void write(String fileName, List<T> data, String sheetName, java.util.function.UnaryOperator<T> converter) {
        Class<T> clazz = inferClass(data);
        List<T> processed = data.stream().map(converter).toList();
        FastExcel.write(fileName, clazz).sheet(localizeSheet(sheetName, null)).doWrite(processed);
    }

    public <T> void write(String fileName, Class<T> clazz, List<T> data, String sheetName, Locale locale) {
        writeInternal(fileName, clazz, data, sheetName, locale, null);
    }

    public <T> void write(String fileName, List<T> data, String sheetName, Locale locale) {
        Class<T> clazz = inferClass(data);
        FastExcel.write(fileName, clazz).sheet(localizeSheet(sheetName, locale)).doWrite(data);
    }

    public <T> void write(String fileName, Class<T> clazz, List<T> data, String sheetName, Locale locale, java.util.function.UnaryOperator<T> converter) {
        writeInternal(fileName, clazz, data, sheetName, locale, converter);
    }

    public <T> void write(String fileName, List<T> data, String sheetName, Locale locale, java.util.function.UnaryOperator<T> converter) {
        Class<T> clazz = inferClass(data);
        List<T> processed = data.stream().map(converter).toList();
        FastExcel.write(fileName, clazz).sheet(localizeSheet(sheetName, locale)).doWrite(processed);
    }

    public <T> void writeWithI18nHeaders(String fileName, Class<T> clazz, List<T> data, String sheetName, Locale locale) {
        List<Field> fields = orderedFields(clazz);
        List<List<String>> head = resolveHeaders(clazz, fields, locale);
        List<List<Object>> rows = buildRows(data, fields);
        FastExcel.write(fileName).head(head).sheet(localizeSheet(sheetName, locale)).doWrite(rows);
    }

    public <T> void writeWithI18nHeaders(String fileName, List<T> data, String sheetName, Locale locale) {
        Class<T> clazz = inferClass(data);
        List<Field> fields = orderedFields(clazz);
        List<List<String>> head = resolveHeaders(clazz, fields, locale);
        List<List<Object>> rows = buildRows(data, fields);
        FastExcel.write(fileName).head(head).sheet(localizeSheet(sheetName, locale)).doWrite(rows);
    }

    public <T> List<T> read(String fileName, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        ReadListener<T> listener = new ReadListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                list.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        };
        FastExcel.read(fileName, clazz, listener).sheet().doRead();
        return list;
    }

    public <T> List<T> read(InputStream in, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        ReadListener<T> listener = new ReadListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                list.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        };
        FastExcel.read(in, clazz, listener).sheet().doRead();
        return list;
    }

    private String localizeSheet(String name, Locale locale) {
        if (!i18nEnabled || messageSource == null) return name;
        Locale loc = locale != null ? locale : defaultLocale;
        if (loc == null) return name;
        try {
            return messageSource.getMessage(name, null, name, loc);
        } catch (Exception e) {
            return name;
        }
    }

    private <T> void writeInternal(String fileName, Class<T> clazz, List<T> data, String sheetName, Locale locale, java.util.function.UnaryOperator<T> converter) {
        List<T> processed = converter != null ? data.stream().map(converter).toList() : data;
        FastExcel.write(fileName, clazz).sheet(localizeSheet(sheetName, locale)).doWrite(processed);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> inferClass(List<T> data) {
        if (data == null || data.isEmpty() || data.get(0) == null) {
            throw new IllegalArgumentException("data must contain at least one non-null element to infer class");
        }
        return (Class<T>) data.get(0).getClass();
    }

    private List<Field> orderedFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            Field[] fs = c.getDeclaredFields();
            list.addAll(Arrays.asList(fs));
            c = c.getSuperclass();
        }
        List<Field> result = new ArrayList<>();
        for (Field f : list) {
            if (f.isSynthetic()) continue;
            ExcelProperty ep = f.getAnnotation(ExcelProperty.class);
            if (ep != null) {
                result.add(f);
            }
        }
        return result;
    }

    private List<List<String>> resolveHeaders(Class<?> clazz, List<Field> fields, Locale locale) {
        List<List<String>> head = new ArrayList<>();
        Locale loc = locale != null ? locale : defaultLocale;
        for (Field f : fields) {
            String code = null;
            ExcelProperty ep = f.getAnnotation(ExcelProperty.class);
            if (ep != null && ep.value() != null && ep.value().length > 0) {
                code = ep.value()[0];
            }
            String fallback = (code != null && !code.isBlank()) ? code : ("excel.header." + clazz.getSimpleName() + "." + f.getName());
            String text = fallback;
            if (i18nEnabled && messageSource != null && loc != null) {
                try {
                    text = messageSource.getMessage(fallback, null, fallback, loc);
                } catch (Exception ignored) {}
            }
            head.add(List.of(text));
        }
        return head;
    }

    private <T> List<List<Object>> buildRows(List<T> data, List<Field> fields) {
        List<List<Object>> rows = new ArrayList<>();
        for (T item : data) {
            List<Object> row = new ArrayList<>(fields.size());
            for (Field f : fields) {
                try {
                    f.setAccessible(true);
                    row.add(f.get(item));
                } catch (IllegalAccessException e) {
                    row.add(null);
                }
            }
            rows.add(row);
        }
        return rows;
    }
}
