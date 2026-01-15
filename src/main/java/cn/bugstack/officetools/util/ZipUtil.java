package cn.bugstack.officetools.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP 压缩工具类
 */
@Slf4j
@Component
public class ZipUtil {

    @Autowired
    private cn.bugstack.officetools.service.R2Service r2Service;

    /**
     * 创建 ZIP (直接使用字节数组)
     *
     * @param files 文件映射 (文件名 -> 内容)
     * @return ZIP 字节数组
     * @throws IOException 创建失败时抛出异常
     */
    public byte[] createZip(Map<String, byte[]> files) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setSize(entry.getValue().length);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("创建 ZIP 失败: {}", e.getMessage(), e);
            throw new IOException("创建 ZIP 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 R2 文件列表创建 ZIP
     *
     * @param fileNames R2 中的文件名列表
     * @return ZIP 字节数组
     * @throws IOException 创建失败时抛出异常
     */
    public byte[] createZipFromR2Files(List<String> fileNames) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (String fileName : fileNames) {
                try {
                    // 从 R2 下载文件
                    byte[] fileContent = r2Service.downloadFile(fileName);

                    // 添加到 ZIP
                    ZipEntry entry = new ZipEntry(fileName);
                    entry.setSize(fileContent.length);
                    zos.putNextEntry(entry);
                    zos.write(fileContent);
                    zos.closeEntry();

                } catch (Exception e) {
                    log.warn("添加文件到 ZIP 失败: {}, 错误: {}", fileName, e.getMessage());
                    // 继续处理其他文件
                }
            }

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("创建 ZIP 失败: {}", e.getMessage(), e);
            throw new IOException("创建 ZIP 失败: " + e.getMessage(), e);
        }
    }
}
