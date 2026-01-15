package cn.bugstack.officetools.service.impl;

import cn.bugstack.officetools.service.AsposeWordService;
import com.aspose.words.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspose.Words 文档处理服务实现类
 *
 * @author bugstack
 * @date 2026-01-13
 */
@Service
public class AsposeWordServiceImpl implements AsposeWordService {

    static {
        // 设置 Aspose.Words 许可证（如果有）
        // License license = new License();
        // license.setLicense("Aspose.Words.lic");
    }

    @Override
    public ByteArrayOutputStream convertDocument(InputStream inputStream, String originalFilename, String targetFormat) throws Exception {
        // 加载文档
        Document doc = new Document(inputStream);

        // 确定保存格式
        int saveFormat;
        switch (targetFormat.toLowerCase()) {
            case "pdf":
                saveFormat = SaveFormat.PDF;
                break;
            case "html":
                saveFormat = SaveFormat.HTML;
                break;
            case "txt":
                saveFormat = SaveFormat.TEXT;
                break;
            case "doc":
                saveFormat = SaveFormat.DOC;
                break;
            case "docx":
                saveFormat = SaveFormat.DOCX;
                break;
            case "rtf":
                saveFormat = SaveFormat.RTF;
                break;
            case "epub":
                saveFormat = SaveFormat.EPUB;
                break;
            default:
                throw new IllegalArgumentException("不支持的目标格式: " + targetFormat);
        }

        // 保存到输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.save(outputStream, saveFormat);

        return outputStream;
    }

    @Override
    public Map<String, Object> getDocumentInfo(InputStream inputStream, String originalFilename) throws Exception {
        Document doc = new Document(inputStream);

        Map<String, Object> info = new HashMap<>();

        // 基本信息
        info.put("fileName", originalFilename);
        info.put("pageCount", doc.getPageCount());
        info.put("wordCount", doc.getBuiltInDocumentProperties().getWords());
        info.put("characterCount", doc.getBuiltInDocumentProperties().getCharacters());
        info.put("paragraphCount", doc.getChildNodes(NodeType.PARAGRAPH, true).getCount());
        info.put("sectionCount", doc.getSections().getCount());

        // 文档属性
        info.put("title", doc.getBuiltInDocumentProperties().getTitle());
        info.put("author", doc.getBuiltInDocumentProperties().getAuthor());
        info.put("subject", doc.getBuiltInDocumentProperties().getSubject());
        info.put("keywords", doc.getBuiltInDocumentProperties().getKeywords());
        info.put("comments", doc.getBuiltInDocumentProperties().getComments());
        info.put("createdTime", doc.getBuiltInDocumentProperties().getCreatedTime());
        info.put("lastSavedTime", doc.getBuiltInDocumentProperties().getLastSavedTime());
        info.put("lastPrinted", doc.getBuiltInDocumentProperties().getLastPrinted());
        info.put("revisionNumber", doc.getBuiltInDocumentProperties().getRevisionNumber());

        // 统计信息
        info.put("lineCount", doc.getBuiltInDocumentProperties().getLines());
        info.put("pageCount", doc.getBuiltInDocumentProperties().getPages());
        info.put("paragraphCount", doc.getBuiltInDocumentProperties().getParagraphs());
        info.put("tableCount", doc.getChildNodes(NodeType.TABLE, true).getCount());
        info.put("imageCount", doc.getChildNodes(NodeType.SHAPE, true).getCount());

        return info;
    }

    @Override
    public ByteArrayOutputStream mergeDocuments(MultipartFile[] files) throws Exception {
        // 创建主文档
        Document mainDoc = new Document();

        // 合并所有文档
        for (MultipartFile file : files) {
            Document doc = new Document(file.getInputStream());
            mainDoc.appendDocument(doc, ImportFormatMode.KEEP_SOURCE_FORMATTING);
        }

        // 保存合并后的文档
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mainDoc.save(outputStream, SaveFormat.DOCX);

        return outputStream;
    }

    @Override
    public ByteArrayOutputStream insertText(InputStream inputStream, String originalFilename, String text, String position) throws Exception {
        Document doc = new Document(inputStream);

        DocumentBuilder builder = new DocumentBuilder(doc);

        if ("start".equalsIgnoreCase(position)) {
            // 移动到文档开头
            builder.moveToDocumentStart();
            builder.writeln(text);
        } else {
            // 默认添加到文档末尾
            builder.moveToDocumentEnd();
            if (doc.getLastSection().getBody().getLastParagraph() != null) {
                // 在最后一节后添加新段落
                builder.insertParagraph();
            }
            builder.write(text);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.save(outputStream, SaveFormat.DOCX);

        return outputStream;
    }

    @Override
    public ByteArrayOutputStream replaceText(InputStream inputStream, String originalFilename, String oldText, String newText, boolean matchCase) throws Exception {
        Document doc = new Document(inputStream);

        FindReplaceOptions options = new FindReplaceOptions();
        options.setMatchCase(matchCase);
        options.setFindWholeWordsOnly(false);

        // 执行替换
        doc.getRange().replace(oldText, newText, options);

        // 保存修改后的文档
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.save(outputStream, SaveFormat.DOCX);

        return outputStream;
    }
}
