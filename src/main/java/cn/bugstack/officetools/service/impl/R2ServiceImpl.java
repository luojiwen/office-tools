package cn.bugstack.officetools.service.impl;

import cn.bugstack.officetools.service.R2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * Cloudflare R2 文件存储服务实现类
 *
 * @author bugstack
 * @date 2026-01-15
 */
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
        s3Client.putObject(putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        // 返回访问 URL
        return buildFileUrl(fileName);
    }

    @Override
    public String uploadBytes(byte[] content, String fileName, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();

        s3Client.putObject(putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(content));

        return buildFileUrl(fileName);
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
