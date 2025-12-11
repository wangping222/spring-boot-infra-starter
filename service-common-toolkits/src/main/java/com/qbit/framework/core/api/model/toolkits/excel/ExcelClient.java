package com.qbit.framework.core.api.model.toolkits.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ExcelClient {


    /**
     * Start a fluent export task
     *
     * @param data Data to export
     * @param <T>  Type of data
     * @return ExcelExportBuilder
     */
    public <T> ExcelExportBuilder<T> export(List<T> data) {
        return new ExcelExportBuilder<>(this, data);
    }

    /**
     * Export data to Excel file and write to response (Annotation based)
     *
     * @param response HttpServletResponse
     * @param fileName File name (without extension)
     * @param head     Class of the data model
     * @param data     List of data to export
     * @param <T>      Type of data
     * @throws IOException If I/O error occurs
     */
    public <T> void export(HttpServletResponse response, String fileName, Class<T> head, List<T> data) throws IOException {
        new ExcelExportBuilder<>(this, data)
                .fileName(fileName)
                .head(head)
                .writeTo(response);
    }

    public static class ExcelExportBuilder<T> {
        private final ExcelClient client;
        private final List<T> data;
        private String fileName;
        private String sheetName;
        private Class<T> head;
        private final List<ExcelCellDescriptor<T>> descriptors = new ArrayList<>();

        public ExcelExportBuilder(ExcelClient client, List<T> data) {
            this.client = client;
            this.data = data;
            this.sheetName = "sheet1";
        }

        public ExcelExportBuilder<T> fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public ExcelExportBuilder<T> sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public ExcelExportBuilder<T> head(Class<T> head) {
            this.head = head;
            return this;
        }

        public ExcelExportBuilder<T> column(String header, Function<T, Object> extractor) {
            this.descriptors.add(ExcelCellDescriptor.of(header, extractor));
            return this;
        }

        public <R> ExcelExportBuilder<T> column(String header, Function<T, R> extractor, Function<R, ?> converter) {
            this.descriptors.add(ExcelCellDescriptor.of(header, t -> converter.apply(extractor.apply(t))));
            return this;
        }

        public ExcelExportBuilder<T> columns(List<ExcelCellDescriptor<T>> descriptors) {
            this.descriptors.addAll(descriptors);
            return this;
        }

        public void writeTo(HttpServletResponse response) throws IOException {
            if (fileName == null) {
                throw new IllegalArgumentException("File name must be specified");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            var writerBuilder = EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet(sheetName);

            if (head != null) {
                writerBuilder.head(head).doWrite(data);
            } else if (!descriptors.isEmpty()) {
                // Dynamic headers
                List<List<String>> heads = new ArrayList<>();
                for (ExcelCellDescriptor<T> desc : descriptors) {
                    heads.add(Collections.singletonList(desc.getHeader()));
                }
                writerBuilder.head(heads);

                // Transform data
                List<List<Object>> rows = new ArrayList<>();
                for (T item : data) {
                    List<Object> row = new ArrayList<>();
                    for (ExcelCellDescriptor<T> desc : descriptors) {
                        row.add(desc.getValueExtractor().apply(item));
                    }
                    rows.add(row);
                }
                writerBuilder.doWrite(rows);
            } else {
                // Try to infer from data if possible, or just write without head
                if (!data.isEmpty()) {
                    writerBuilder.head(data.get(0).getClass()).doWrite(data);
                } else {
                    writerBuilder.doWrite(data);
                }
            }
        }
    }

    /**
     * Read data from Excel file
     *
     * @param inputStream  Input stream of the Excel file
     * @param head         Class of the data model
     * @param readListener Listener for reading data
     * @param <T>          Type of data
     */
    public <T> void read(InputStream inputStream, Class<T> head, ReadListener<T> readListener) {
        EasyExcel.read(inputStream, head, readListener).sheet().doRead();
    }
}
