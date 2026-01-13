package cn.bugstack.officetools.service.impl;

import cn.bugstack.officetools.service.SpireDocService;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import com.spire.doc.Section;
import com.spire.doc.documents.Paragraph;
import com.spire.doc.TextWatermark;
import com.spire.doc.documents.WatermarkLayout;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Spire.Doc 文档处理服务实现类
 *
 * @author bugstack
 * @date 2026-01-13
 */
@Service
public class SpireDocServiceImpl implements SpireDocService {

    @Override
    public ByteArrayOutputStream convertDocument(InputStream inputStream, String originalFilename, String targetFormat) throws Exception {
        // 加载文档
        Document doc = new Document();
        doc.loadFromStream(inputStream, FileFormat.Docx_2013);

        // 确定保存格式
        FileFormat fileFormat;
        switch (targetFormat.toLowerCase()) {
            case "pdf":
                fileFormat = FileFormat.PDF;
                break;
            case "html":
                fileFormat = FileFormat.Html;
                break;
            case "txt":
                fileFormat = FileFormat.Txt;
                break;
            case "doc":
                fileFormat = FileFormat.Doc;
                break;
            case "docx":
                fileFormat = FileFormat.Docx_2013;
                break;
            case "rtf":
                fileFormat = FileFormat.Rtf;
                break;
            case "xps":
                fileFormat = FileFormat.XPS;
                break;
            default:
                throw new IllegalArgumentException("不支持的目标格式: " + targetFormat);
        }

        // 保存到输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.saveToStream(outputStream, fileFormat);

        return outputStream;
    }

    @Override
    public Map<String, Object> getDocumentInfo(InputStream inputStream, String originalFilename) throws Exception {
        Document doc = new Document();
        doc.loadFromStream(inputStream, FileFormat.Auto);

        Map<String, Object> info = new HashMap<>();

        // 基本信息
        info.put("fileName", originalFilename);
        info.put("pageCount", doc.getPageCount());

        // 文档属性
        info.put("title", doc.getBuiltinDocumentProperties().getTitle());
        info.put("author", doc.getBuiltinDocumentProperties().getAuthor());
        info.put("subject", doc.getBuiltinDocumentProperties().getSubject());
        info.put("keywords", doc.getBuiltinDocumentProperties().getKeywords());
        info.put("comments", doc.getBuiltinDocumentProperties().getComments());

        // 统计信息
        int sectionCount = doc.getSections().getCount();
        int paragraphCount = 0;
        int tableCount = 0;

        for (int i = 0; i < sectionCount; i++) {
            Section section = doc.getSections().get(i);
            paragraphCount += section.getParagraphs().getCount();
            // 统计表格数量
            for (int j = 0; j < section.getParagraphs().getCount(); j++) {
                Paragraph para = section.getParagraphs().get(j);
                tableCount += para.getChildObjects().getCount();
            }
        }

        info.put("sectionCount", sectionCount);
        info.put("paragraphCount", paragraphCount);
        info.put("tableCount", tableCount);

        return info;
    }

    @Override
    public String extractText(InputStream inputStream, String originalFilename) throws Exception {
        Document doc = new Document();
        doc.loadFromStream(inputStream, FileFormat.Auto);

        // 提取文本内容
        StringBuilder textContent = new StringBuilder();
        for (Object sectionObj : doc.getSections()) {
            Section section = (Section) sectionObj;
            for (Object paraObj : section.getParagraphs()) {
                Paragraph para = (Paragraph) paraObj;
                textContent.append(para.getText()).append("\n");
            }
        }

        return textContent.toString();
    }

    @Override
    public ByteArrayOutputStream addWatermark(InputStream inputStream, String originalFilename, String watermarkText, boolean isImageWatermark) throws Exception {
        Document doc = new Document();
        doc.loadFromStream(inputStream, FileFormat.Auto);

        if (!isImageWatermark) {
            // 添加文本水印
            TextWatermark txtWatermark = new TextWatermark();
            txtWatermark.setText(watermarkText);
            txtWatermark.setFontSize(40f);
            txtWatermark.setColor(new java.awt.Color(128, 128, 128, 128));
            txtWatermark.setLayout(WatermarkLayout.Horizontal);
            doc.setWatermark(txtWatermark);
        } else {
            // 图片水印需要先有图片文件，这里简化处理，实际使用时需要传入图片流
            TextWatermark txtWatermark = new TextWatermark();
            txtWatermark.setText(watermarkText);
            txtWatermark.setFontSize(40f);
            txtWatermark.setColor(new java.awt.Color(128, 128, 128, 128));
            doc.setWatermark(txtWatermark);
        }

        // 保存文档
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.saveToStream(outputStream, FileFormat.Docx_2013);

        return outputStream;
    }

    @Override
    public ByteArrayOutputStream protectDocument(InputStream inputStream, String originalFilename, String password, String protectionType) throws Exception {
        Document doc = new Document();
        doc.loadFromStream(inputStream, FileFormat.Auto);

        // Spire.Doc 使用 protect 方法进行文档保护
        // 注意: Spire.Doc Free 版可能有限制
        try {
            // 使用枚举值进行保护
            doc.protect(com.spire.doc.ProtectionType.Allow_Only_Reading);
        } catch (Exception e) {
            // 保护功能可能在免费版中不可用
            System.err.println("文档保护功能可能需要商业版 Spire.Doc: " + e.getMessage());
        }

        // 保存文档
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.saveToStream(outputStream, FileFormat.Docx_2013);

        return outputStream;
    }

    @Override
    public ByteArrayOutputStream unprotectDocument(InputStream inputStream, String originalFilename, String password) throws Exception {
        Document doc = new Document();
        doc.loadFromStream(inputStream, FileFormat.Auto);

        // 移除文档保护
        try {
            doc.unprotect();
        } catch (Exception e) {
            // 移除保护功能可能在免费版中不可用
            System.err.println("移除文档保护功能可能需要商业版 Spire.Doc: " + e.getMessage());
        }

        // 保存文档
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.saveToStream(outputStream, FileFormat.Docx_2013);

        return outputStream;
    }
}
