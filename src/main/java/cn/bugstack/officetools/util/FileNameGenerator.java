package cn.bugstack.officetools.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件名生成工具
 */
@Component
public class FileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    /**
     * 生成 PDF 文件名
     *
     * @param originalFilename 原始文件名
     * @return PDF 文件名 (带时间戳和唯一 ID)
     */
    public String generatePdfFileName(String originalFilename) {
        String baseName = extractBaseName(originalFilename);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return baseName + "_" + timestamp + "_" + uniqueId + ".pdf";
    }

    /**
     * 生成 ZIP 文件名
     *
     * @return ZIP 文件名 (带时间戳和唯一 ID)
     */
    public String generateZipFileName() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return "converted_pdfs_" + timestamp + "_" + uniqueId + ".zip";
    }

    /**
     * 从文件名提取基础名称（去掉扩展名）
     *
     * @param filename 文件名
     * @return 基础名称
     */
    private String extractBaseName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "document";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }

        return filename;
    }
}
