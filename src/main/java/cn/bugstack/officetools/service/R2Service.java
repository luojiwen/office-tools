package cn.bugstack.officetools.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Cloudflare R2 文件存储服务接口
 *
 * @author bugstack
 * @date 2026-01-15
 */
public interface R2Service {

    /**
     * 上传文件到 R2
     *
     * @param file 要上传的文件
     * @return 文件的访问 URL
     * @throws IOException 上传失败时抛出异常
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * 上传文件到 R2，指定文件名
     *
     * @param file     要上传的文件
     * @param fileName 目标文件名（可选，为 null 则使用 UUID）
     * @return 文件的访问 URL
     * @throws IOException 上传失败时抛出异常
     */
    String uploadFile(MultipartFile file, String fileName) throws IOException;

    /**
     * 上传字节数组到 R2
     *
     * @param content     文件内容
     * @param fileName    文件名
     * @param contentType 内容类型
     * @return 文件的访问 URL
     */
    String uploadBytes(byte[] content, String fileName, String contentType);

    /**
     * 删除 R2 中的文件
     *
     * @param fileName 要删除的文件名
     */
    void deleteFile(String fileName);

    /**
     * 检查文件是否存在
     *
     * @param fileName 文件名
     * @return 文件是否存在
     */
    boolean fileExists(String fileName);
}
