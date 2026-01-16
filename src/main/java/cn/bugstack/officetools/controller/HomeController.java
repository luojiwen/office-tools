package cn.bugstack.officetools.controller;

import cn.bugstack.officetools.domain.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 * 处理根路径请求，提供 API 基本信息
 *
 * @author bugstack
 * @date 2026-01-15
 */
@RestController
public class HomeController {

    /**
     * 首页 - 返回 API 基本信息
     */
    @GetMapping("/")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> info = new HashMap<>();

        // 基本信息
        info.put("name", "Office Tools API");
        info.put("version", "1.0.0");
        info.put("description", "Office 文档处理服务，提供 Word 文档转换、操作和 Cloudflare R2 存储功能");

        // API 端点
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("文档转换 (同步)", "POST /api/convert/word-to-pdf/sync/single");
        endpoints.put("文档转换 (异步)", "POST /api/convert/word-to-pdf/async/single");
        endpoints.put("批量转换 (同步)", "POST /api/convert/word-to-pdf/sync/batch");
        endpoints.put("批量转换 (异步)", "POST /api/convert/word-to-pdf/async/batch");
        endpoints.put("查询任务状态", "GET /api/convert/task/{taskId}");
        endpoints.put("下载转换结果", "GET /api/convert/download/{taskId}");
        endpoints.put("健康检查", "GET /actuator/health");
        info.put("endpoints", endpoints);

        // 功能特性
        Map<String, String> features = new HashMap<>();
        features.put("Aspose.Words", "Word 转 PDF/HTML/TXT、文档合并、文本替换");
        features.put("Spire.Doc", "文档信息、文本提取、水印、加密保护");
        features.put("Cloudflare R2", "文件上传、下载、删除、存在性检查");
        info.put("features", features);

        // 使用说明
        info.put("usage", "使用 POST 请求上传文件进行转换，支持 .doc 和 .docx 格式");

        return new ApiResponse<>(true, "Office Tools API 运行中", info);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "office-tools");
        status.put("timestamp", java.time.LocalDateTime.now().toString());

        return new ApiResponse<>(true, "服务正常", status);
    }

    /**
     * 内存监控接口
     */
    @GetMapping("/memory")
    public ApiResponse<Map<String, Object>> memory() {
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> memoryInfo = new HashMap<>();

        // JVM 堆内存信息
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        memoryInfo.put("jvmMaxMemory", maxMemory / 1024 / 1024 + " MB");
        memoryInfo.put("jvmTotalMemory", totalMemory / 1024 / 1024 + " MB");
        memoryInfo.put("jvmFreeMemory", freeMemory / 1024 / 1024 + " MB");
        memoryInfo.put("jvmUsedMemory", usedMemory / 1024 / 1024 + " MB");

        // 内存使用百分比
        double usagePercent = (usedMemory * 100.0) / maxMemory;
        memoryInfo.put("heapUsagePercent", String.format("%.2f%%", usagePercent));

        // 系统内存信息
        memoryInfo.put("availableProcessors", runtime.availableProcessors());

        // 元空间信息 (通过 JVM MXBean 获取)
        try {
            java.lang.management.MemoryMXBean memoryMxBean = java.lang.management.ManagementFactory.getMemoryMXBean();
            java.lang.management.MemoryUsage metaspaceUsage = memoryMxBean.getHeapMemoryUsage(); // 注意：这会返回堆内存，我们需要非堆内存

            // 获取非堆内存（包含元空间）
            java.lang.management.MemoryUsage nonHeapMemoryUsage = memoryMxBean.getNonHeapMemoryUsage();

            memoryInfo.put("metaspaceCommitted", (nonHeapMemoryUsage.getCommitted() / 1024 / 1024) + " MB");
            memoryInfo.put("metaspaceUsed", (nonHeapMemoryUsage.getUsed() / 1024 / 1024) + " MB");
            memoryInfo.put("metaspaceMax", (nonHeapMemoryUsage.getMax() / 1024 / 1024) + " MB");

            // 计算元空间使用百分比
            if (nonHeapMemoryUsage.getMax() > 0) {
                double metaspacePercent = (nonHeapMemoryUsage.getUsed() * 100.0) / nonHeapMemoryUsage.getMax();
                memoryInfo.put("metaspaceUsagePercent", String.format("%.2f%%", metaspacePercent));
            }
        } catch (Exception e) {
            memoryInfo.put("metaspaceError", "无法获取元空间信息");
        }

        // 内存使用警告
        String warning;
        if (usagePercent > 80) {
            warning = "⚠️ 堆内存使用率过高！";
        } else if (usagePercent > 60) {
            warning = "⚠️ 堆内存使用率较高";
        } else {
            warning = "✅ 堆内存使用正常";
        }
        memoryInfo.put("heapWarning", warning);

        return new ApiResponse<>(true, "内存信息", memoryInfo);
    }
}
