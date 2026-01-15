package cn.bugstack.officetools.service.impl;

import cn.bugstack.officetools.service.R2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Cloudflare R2 文件存储服务实现类
 *
 * @author bugstack
 * @date 2026-01-15
 */
@Slf4j
@Service
public class R2ServiceImpl implements R2Service {

    @Autowired
    private S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-domain:}")
    private String publicDomain;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, null);
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        // 如果没有指定文件名，使用 UUID 生成
        if (fileName == null || fileName.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            fileName = UUID.randomUUID().toString() + extension;
        }

        // 构建上传请求
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // 执行上传
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        // 返回访问 URL
        return buildFileUrl(fileName);
    }

    @Override
    public String uploadBytes(byte[] content, String fileName, String contentType) {
        long totalStart = System.currentTimeMillis();

        // 构建上传请求
        long buildStart = System.currentTimeMillis();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();
        log.info("【性能监控-R2】构建请求耗时: {}ms",
                System.currentTimeMillis() - buildStart);

        // 执行上传
        long uploadStart = System.currentTimeMillis();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
        long uploadTime = System.currentTimeMillis() - uploadStart;

        long totalTime = System.currentTimeMillis() - totalStart;
        // 计算上传速度 (KB/s)
        double speedKB = (content.length / 1024.0) / (uploadTime / 1000.0);
        log.info("【性能监控-R2】上传耗时: {}ms, 文件: {}, 大小: {} bytes, 速度: {:.2f} KB/s, 总耗时: {}ms",
                uploadTime, fileName, content.length, speedKB, totalTime);

        return buildFileUrl(fileName);
    }

    @Override
    public byte[] downloadFile(String fileName) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try (InputStream is = s3Client.getObject(getObjectRequest);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            throw new IOException("下载文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFileStream(String fileName) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try {
            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("下载文件流失败: {}", e.getMessage(), e);
            throw new IOException("下载文件流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    @Override
    public boolean fileExists(String fileName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * 构建文件的访问 URL
     *
     * @param fileName 文件名
     * @return 访问 URL
     */
    private String buildFileUrl(String fileName) {
        // 如果配置了公共域名，使用公共域名
        if (publicDomain != null && !publicDomain.isEmpty()) {
            return publicDomain + "/" + fileName;
        }

        // 否则返回相对路径（适用于自定义域名场景）
        return "/" + fileName;
    }
}
