package cn.bugstack.officetools.util;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 临时文件存储（带内存限制）
 */
@Slf4j
@Component
public class TempFileStorage {

    private final Map<String, TempFileEntry> storage = new ConcurrentHashMap<>();
    private final AtomicLong currentMemoryUsage = new AtomicLong(0);

    @Value("${conversion.temp-file.expire-minutes:30}")
    private int expireMinutes;

    @Value("${conversion.temp-file.max-memory-mb:500}")
    private long maxMemoryMb;

    private long maxMemoryBytes;

    @Value("${conversion.temp-file.max-count:100}")
    private int maxFileCount;

    /**
     * 初始化后设置内存限制
     */
    @PostConstruct
    public void init() {
        maxMemoryBytes = maxMemoryMb * 1024 * 1024;
        log.info("临时文件存储初始化 - 最大内存: {}MB, 最大文件数: {}", maxMemoryMb, maxFileCount);
    }

    /**
     * 存储临时文件
     *
     * @param fileName 文件名
     * @param content  文件内容
     * @throws IllegalStateException 如果内存不足
     */
    public void store(String fileName, byte[] content) {
        // 检查文件数量限制
        if (storage.size() >= maxFileCount) {
            cleanupOldFiles();
            if (storage.size() >= maxFileCount) {
                throw new IllegalStateException("临时存储已满，请稍后重试");
            }
        }

        // 检查内存限制
        long newMemoryUsage = currentMemoryUsage.get() + content.length;
        if (newMemoryUsage > maxMemoryBytes) {
            cleanupOldFiles();
            newMemoryUsage = currentMemoryUsage.get() + content.length;
            if (newMemoryUsage > maxMemoryBytes) {
                throw new IllegalStateException("临时存储内存不足，当前使用: " +
                        (currentMemoryUsage.get() / 1024 / 1024) + "MB, 最大: " + maxMemoryMb + "MB");
            }
        }

        TempFileEntry entry = new TempFileEntry(
                fileName,
                content,
                LocalDateTime.now().plusMinutes(expireMinutes),
                content.length
        );

        storage.put(fileName, entry);
        currentMemoryUsage.addAndGet(content.length);

        log.debug("存储临时文件: {}, 大小: {}KB, 当前内存使用: {}MB",
                fileName, content.length / 1024, currentMemoryUsage.get() / 1024 / 1024);
    }

    /**
     * 获取临时文件
     *
     * @param fileName 文件名
     * @return 文件内容，如果文件不存在或已过期返回 null
     */
    public byte[] get(String fileName) {
        TempFileEntry entry = storage.get(fileName);

        if (entry == null) {
            return null;
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(entry.getExpireTime())) {
            remove(fileName);
            log.debug("临时文件已过期并移除: {}", fileName);
            return null;
        }

        return entry.getContent();
    }

    /**
     * 删除临时文件
     *
     * @param fileName 文件名
     */
    public void remove(String fileName) {
        TempFileEntry entry = storage.remove(fileName);
        if (entry != null) {
            currentMemoryUsage.addAndGet(-entry.getContentLength());
            log.debug("移除临时文件: {}, 释放内存: {}KB", fileName, entry.getContentLength() / 1024);
        }
    }

    /**
     * 定时清理过期文件
     */
    @Scheduled(fixedRate = 300000) // 每 5 分钟清理一次
    public void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        int removedCount = 0;
        long freedMemory = 0;

        for (Map.Entry<String, TempFileEntry> entry : storage.entrySet()) {
            if (now.isAfter(entry.getValue().getExpireTime())) {
                freedMemory += entry.getValue().getContentLength();
                storage.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            currentMemoryUsage.addAndGet(-freedMemory);
            log.info("清理过期临时文件: {} 个, 释放内存: {}MB", removedCount, freedMemory / 1024 / 1024);
        }
    }

    /**
     * 清理旧文件（当存储空间不足时）
     */
    private void cleanupOldFiles() {
        // 按过期时间排序，删除最老的文件
        storage.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().getExpireTime().compareTo(e2.getValue().getExpireTime()))
                .limit(storage.size() / 4) // 删除 25% 的旧文件
                .forEach(entry -> {
                    remove(entry.getKey());
                });

        log.info("主动清理旧文件，当前文件数: {}, 内存使用: {}MB",
                storage.size(), currentMemoryUsage.get() / 1024 / 1024);
    }

    /**
     * 获取存储大小
     *
     * @return 存储的文件数量
     */
    public int size() {
        return storage.size();
    }

    /**
     * 获取当前内存使用量（MB）
     */
    public long getCurrentMemoryUsageMb() {
        return currentMemoryUsage.get() / 1024 / 1024;
    }

    @Data
    @AllArgsConstructor
    private static class TempFileEntry {
        private String fileName;
        private byte[] content;
        private LocalDateTime expireTime;
        private long contentLength;
    }
}
