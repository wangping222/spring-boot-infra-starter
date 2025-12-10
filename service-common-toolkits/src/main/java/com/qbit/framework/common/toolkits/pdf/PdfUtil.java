package com.qbit.framework.common.toolkits.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import lombok.experimental.UtilityClass;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PDF工具
 *
 * @author ppp
 * @date 2024/1/22
 */
@UtilityClass
public class PdfUtil {

    static {
        // Velocity初始化
        Velocity.setProperty(RuntimeConstants.OUTPUT_ENCODING, StandardCharsets.UTF_8);
        Velocity.setProperty(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8);
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
    }


    /**
     * 据模板生成pfd格式文件
     *
     * @param context      上下文对象
     * @param template     pdf模板
     * @param outputStream 生成ofd文件输出流
     */
    public static void pdfFile(Context context, String template, OutputStream outputStream) {
        try (PdfWriter pdfWriter = new PdfWriter(outputStream)) {
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A4);
            ConverterProperties properties = new ConverterProperties();
            FontProvider fontProvider = new FontProvider();
            // 加载 PingFangMedium 字体
            String fontPath = "templates/font/PingFangMedium.ttf";
            fontProvider.addFont(fontPath);
            properties.setFontProvider(fontProvider);
            Template pfdTemplate = Velocity.getTemplate(template, "UTF-8");
            StringWriter writer = new StringWriter();
            pfdTemplate.merge(context, writer);
            HtmlConverter.convertToPdf(writer.toString(), pdfDocument, properties);
            pdfDocument.close();
        } catch (Exception e) {
            throw new RuntimeException("PFD文件生成失败", e);
        }
    }

    /**
     * 根据多模板生成多页的pfd格式文件
     *
     * @param context      上下文对象
     * @param templates    pdf模板
     * @param outputStream 生成ofd文件输出流
     */
    public static void pdfFile(Context context, List<String> templates, OutputStream outputStream) {
        try (PdfWriter pdfWriter = new PdfWriter(outputStream)) {
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A4);
            ConverterProperties properties = new ConverterProperties();
            FontProvider fontProvider = new FontProvider();
            // 加载 PingFangMedium 字体
            String fontPath = "templates/font/PingFangMedium.ttf";
            fontProvider.addFont(fontPath);
            properties.setFontProvider(fontProvider);
            StringWriter totalWriter = new StringWriter();
            for (String template : templates) {
                Template pfdTemplate = Velocity.getTemplate(template, "UTF-8");
                StringWriter writer = new StringWriter();
                pfdTemplate.merge(context, writer);
                totalWriter.append(writer.toString());
            }
            HtmlConverter.convertToPdf(totalWriter.toString(), pdfDocument, properties);
            pdfDocument.close();
        } catch (Exception e) {
            throw new RuntimeException("PFD文件生成失败", e);
        }
    }

}
