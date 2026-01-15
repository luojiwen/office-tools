package cn.bugstack.officetools.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 内存中的 MultipartFile 表示
 * 用于异步任务中传递文件数据，避免原始 InputStream 关闭问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryMultipartFile {

    /**
     * 文件内容
     */
    private byte[] content;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 文件大小
     */
    private long size;

    /**
     * 从 MultipartFile 创建内存副本
     */
    public static MemoryMultipartFile from(MultipartFile file) throws IOException {
        return new MemoryMultipartFile(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * 创建输入流（每次调用返回新的流）
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    /**
     * 判断文件是否为空
     */
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }
}
