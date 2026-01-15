package cn.bugstack.officetools.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 转换结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResult {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    // 单文件结果
    /**
     * PDF 文件名
     */
    private String pdfFileName;

    /**
     * PDF 文件 URL
     */
    private String pdfUrl;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 内容类型
     */
    private String contentType;

    // 批量结果
    /**
     * ZIP 文件名
     */
    private String zipFileName;

    /**
     * ZIP 文件 URL
     */
    private String zipUrl;

    /**
     * 总文件数
     */
    private Integer totalFiles;

    /**
     * 转换后的文件列表
     */
    private List<ConvertedFileInfo> convertedFiles;

    // 异步回调
    /**
     * 回调 URL
     */
    private String callbackUrl;
}
