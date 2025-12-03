package com.cool.core.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * PDF工具类 - 将HTML转换为PDF
 */
@Slf4j
public class PdfUtil {

    /**
     * 将HTML字符串转换为PDF字节数组
     *
     * @param html HTML内容
     * @return PDF字节数组
     */
    public static byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            
            // 添加中文字体支持 - 必须在withHtmlContent之前注册
            // 使用lambda表达式提供字体流，这样流会在需要时才被创建和使用
            InputStream fontStream = PdfUtil.class.getResourceAsStream("/fonts/SimSun.ttf");
            if (fontStream != null) {
                builder.useFont(() -> PdfUtil.class.getResourceAsStream("/fonts/SimSun.ttf"), 
                               "SimSun", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
                log.info("成功注册中文字体 SimSun.ttf");
                fontStream.close(); // 关闭测试流
            } else {
                log.warn("未找到中文字体文件 /fonts/SimSun.ttf，将使用默认字体");
            }
            
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            
            return os.toByteArray();
        } catch (Exception e) {
            log.error("HTML转PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 包装HTML为完整的HTML文档
     *
     * @param bodyContent HTML内容
     * @return 完整的HTML文档
     */
    public static String wrapHtml(String bodyContent) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\"/>" +
                "<style>" +
                "body { font-family: 'SimSun', serif; font-size: 12px; margin: 20px; }" +
                "table { border-collapse: collapse; width: 100%; margin: 10px 0; }" +
                "th, td { border: 1px solid #000; padding: 8px; text-align: left; }" +
                "th { background-color: #f5f5f5; font-weight: bold; }" +
                "h1, h2, h3 { text-align: center; }" +
                ".text-right { text-align: right; }" +
                ".text-center { text-align: center; }" +
                ".bold { font-weight: bold; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                bodyContent +
                "</body>" +
                "</html>";
    }
}
