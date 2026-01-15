package cn.bugstack.officetools.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Cloudflare R2 配置类
 * R2 兼容 S3 API，使用 AWS SDK 进行连接
 */
@Configuration
public class R2Config {

    @Value("${r2.endpoint}")
    private String endpoint;

    @Value("${r2.access-key-id}")
    private String accessKeyId;

    @Value("${r2.secret-access-key}")
    private String secretAccessKey;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        // R2 使用 auto 区域，或根据实际情况调整
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto")) // R2 使用 auto 作为区域
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // R2 需要启用路径样式访问
                        .build())
                .build();
    }
}