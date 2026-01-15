package cn.bugstack.officetools.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 转换文件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertedFileInfo {
    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 转换后的 PDF 文件名
     */
    private String pdfName;

    /**
     * PDF 文件 URL
     */
    private String pdfUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 错误信息（如果转换失败）
     */
    private String error;
}
