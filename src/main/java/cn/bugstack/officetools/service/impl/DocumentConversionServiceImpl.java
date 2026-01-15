package cn.bugstack.officetools.service.impl;

import cn.bugstack.officetools.domain.dto.*;
import cn.bugstack.officetools.domain.model.MemoryMultipartFile;
import cn.bugstack.officetools.service.*;
import cn.bugstack.officetools.util.FileNameGenerator;
import cn.bugstack.officetools.util.TempFileStorage;
import cn.bugstack.officetools.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档转换服务实现类
 */
@Slf4j
@Service
public class DocumentConversionServiceImpl implements DocumentConversionService {

    @Autowired
    private AsposeWordService asposeWordService;

    @Autowired
    private R2Service r2Service;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private FileNameGenerator fileNameGenerator;

    @Autowired
    private ZipUtil zipUtil;

    @Autowired
    private TempFileStorage tempFileStorage;

    @Value("${conversion.file.max-count:20}")
    private int maxFileCount;

    @Value("${conversion.file.max-size:20MB}")
    private String maxFileSize;

    @Value("${conversion.file.max-size-bytes:20971520}")
    private long maxFileSizeBytes;

    @Override
    public ConversionResult convertSingleSync(MultipartFile file, boolean returnFile) throws Exception {
        long totalStart = System.currentTimeMillis();

        // 参数验证
        long validateStart = System.currentTimeMillis();
        validateFile(file);
        log.info("【性能监控】文件验证耗时: {}ms, 文件名: {}, 文件大小: {} bytes",
                System.currentTimeMillis() - validateStart, file.getOriginalFilename(), file.getSize());

        // 使用 AsposeWordService 转换为 PDF
        long convertStart = System.currentTimeMillis();
        ByteArrayOutputStream pdfStream = (ByteArrayOutputStream) asposeWordService.convertDocument(
                file.getInputStream(),
                file.getOriginalFilename(),
                "pdf"
        );
        long convertTime = System.currentTimeMillis() - convertStart;
        log.info("【性能监控】Aspose.Words 转换耗时: {}ms, 输入文件: {}, 输出大小: {} bytes",
                convertTime, file.getOriginalFilename(), pdfStream.size());

        // 生成 PDF 文件名（带时间戳）
        long fileNameStart = System.currentTimeMillis();
        String pdfFileName = fileNameGenerator.generatePdfFileName(file.getOriginalFilename());
        log.info("【性能监控】生成文件名耗时: {}ms, 文件名: {}",
                System.currentTimeMillis() - fileNameStart, pdfFileName);

        // 上传到 R2
        long uploadStart = System.currentTimeMillis();
        String pdfUrl = r2Service.uploadBytes(pdfStream.toByteArray(), pdfFileName, "application/pdf");
        log.info("【性能监控】R2 上传耗时: {}ms, 文件名: {}, URL: {}",
                System.currentTimeMillis() - uploadStart, pdfFileName, pdfUrl);

        // 如果需要直接返回文件流，保存到临时存储
        long storageStart = System.currentTimeMillis();
        if (returnFile) {
            tempFileStorage.store(pdfFileName, pdfStream.toByteArray());
        }
        log.info("【性能监控】临时存储耗时: {}ms",
                System.currentTimeMillis() - storageStart);

        // 构建结果
        ConversionResult result = ConversionResult.builder()
                .success(true)
                .message("转换成功")
                .pdfFileName(pdfFileName)
                .pdfUrl(pdfUrl)
                .fileSize((long) pdfStream.size())
                .contentType("application/pdf")
                .build();

        long totalTime = System.currentTimeMillis() - totalStart;
        log.info("【性能监控】单文件转换总耗时: {}ms, 文件: {}, 大小: {} -> {} bytes",
                totalTime, file.getOriginalFilename(), file.getSize(), pdfStream.size());

        return result;
    }

    @Override
    public ConversionResult convertBatchSync(MultipartFile[] files, boolean returnFile) throws Exception {
        // 参数验证
        validateFiles(files);

        List<ConvertedFileInfo> convertedFiles = new ArrayList<>();
        List<String> pdfFileNames = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        // 批量转换每个文件
        for (MultipartFile file : files) {
            try {
                // 转换单个文件
                ByteArrayOutputStream pdfStream = (ByteArrayOutputStream) asposeWordService.convertDocument(
                        file.getInputStream(),
                        file.getOriginalFilename(),
                        "pdf"
                );

                // 生成文件名
                String pdfFileName = fileNameGenerator.generatePdfFileName(file.getOriginalFilename());
                pdfFileNames.add(pdfFileName);

                // 上传到 R2
                String pdfUrl = r2Service.uploadBytes(pdfStream.toByteArray(), pdfFileName, "application/pdf");

                // 记录结果
                convertedFiles.add(ConvertedFileInfo.builder()
                        .originalName(file.getOriginalFilename())
                        .pdfName(pdfFileName)
                        .pdfUrl(pdfUrl)
                        .fileSize((long) pdfStream.size())
                        .build());

                successCount++;

            } catch (Exception e) {
                // 记录失败但继续处理其他文件
                log.error("文件转换失败: {}, 错误: {}", file.getOriginalFilename(), e.getMessage(), e);
                convertedFiles.add(ConvertedFileInfo.builder()
                        .originalName(file.getOriginalFilename())
                        .error(e.getMessage())
                        .build());
                failCount++;
            }
        }

        // 打包成 ZIP
        String zipFileName = null;
        String zipUrl = null;

        if (!pdfFileNames.isEmpty()) {
            // 生成 ZIP 文件名
            zipFileName = fileNameGenerator.generateZipFileName();

            // 从 R2 下载所有 PDF 并打包
            byte[] zipBytes = zipUtil.createZipFromR2Files(pdfFileNames);

            // 上传 ZIP 到 R2
            zipUrl = r2Service.uploadBytes(zipBytes, zipFileName, "application/zip");

            // 临时存储用于下载
            if (returnFile) {
                tempFileStorage.store(zipFileName, zipBytes);
            }
        }

        // 构建结果
        ConversionResult result = ConversionResult.builder()
                .success(true)
                .message(String.format("批量转换完成: 成功 %d 个, 失败 %d 个", successCount, failCount))
                .zipFileName(zipFileName)
                .zipUrl(zipUrl)
                .totalFiles(files.length)
                .convertedFiles(convertedFiles)
                .build();

        log.info("批量转换完成: 总数 {}, 成功 {}, 失败 {}", files.length, successCount, failCount);
        return result;
    }

    @Override
    public String convertSingleAsync(MultipartFile file, String callbackUrl) {
        // 参数验证
        try {
            validateFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("文件验证失败: " + e.getMessage());
        }

        try {
            // 将文件内容读取到内存中，避免异步执行时 InputStream 关闭
            MemoryMultipartFile memoryFile = MemoryMultipartFile.from(file);

            // 创建任务
            String taskId = taskManager.createTask(TaskType.SINGLE, 1);

            // 异步处理 - 使用内存中的文件数据
            processConversionAsync(taskId, new MemoryMultipartFile[]{memoryFile}, false, callbackUrl);

            return taskId;
        } catch (IOException e) {
            throw new IllegalArgumentException("读取文件内容失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertBatchAsync(MultipartFile[] files, String callbackUrl) {
        // 参数验证
        try {
            validateFiles(files);
        } catch (Exception e) {
            throw new IllegalArgumentException("文件验证失败: " + e.getMessage());
        }

        try {
            // 将所有文件内容读取到内存中
            List<MemoryMultipartFile> memoryFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                memoryFiles.add(MemoryMultipartFile.from(file));
            }

            // 创建任务
            String taskId = taskManager.createTask(TaskType.BATCH, files.length);

            // 异步处理 - 使用内存中的文件数据
            processConversionAsync(taskId, memoryFiles.toArray(new MemoryMultipartFile[0]), true, callbackUrl);

            return taskId;
        } catch (IOException e) {
            throw new IllegalArgumentException("读取文件内容失败: " + e.getMessage(), e);
        }
    }

    @Async("conversionTaskExecutor")
    public void processConversionAsync(String taskId, MemoryMultipartFile[] memoryFiles, boolean isBatch, String callbackUrl) {
        try {
            // 更新任务状态为处理中
            taskManager.updateTaskStatus(taskId, TaskStatus.PROCESSING, 10);

            ConversionResult result;
            if (isBatch) {
                // 批量转换
                result = convertBatchMemoryFiles(memoryFiles, false);
                taskManager.updateTaskStatus(taskId, TaskStatus.PROCESSING, 90);
            } else {
                // 单文件转换
                result = convertSingleMemoryFile(memoryFiles[0], false);
                taskManager.updateTaskStatus(taskId, TaskStatus.PROCESSING, 90);
            }

            // 完成任务
            taskManager.updateTaskStatus(taskId, TaskStatus.COMPLETED, 100);
            result.setCallbackUrl(callbackUrl);
            taskManager.setTaskResult(taskId, result);

            log.info("异步任务完成: {}", taskId);

        } catch (Exception e) {
            // 任务失败
            log.error("异步任务失败: {}, 错误: {}", taskId, e.getMessage(), e);
            taskManager.updateTaskStatus(taskId, TaskStatus.FAILED, 0);
            taskManager.setTaskError(taskId, e.getMessage());
        }
    }

    /**
     * 转换单个内存文件
     */
    private ConversionResult convertSingleMemoryFile(MemoryMultipartFile memoryFile, boolean returnFile) throws Exception {
        // 创建适配的 MultipartFile
        MultipartFile adaptedFile = new AdaptedMultipartFile(memoryFile);

        // 调用原有的转换方法
        return convertSingleSync(adaptedFile, returnFile);
    }

    /**
     * 批量转换内存文件
     */
    private ConversionResult convertBatchMemoryFiles(MemoryMultipartFile[] memoryFiles, boolean returnFile) throws Exception {
        // 创建适配的 MultipartFile 数组
        MultipartFile[] adaptedFiles = new MultipartFile[memoryFiles.length];
        for (int i = 0; i < memoryFiles.length; i++) {
            adaptedFiles[i] = new AdaptedMultipartFile(memoryFiles[i]);
        }

        // 调用原有的批量转换方法
        return convertBatchSync(adaptedFiles, returnFile);
    }

    /**
     * 适配器类：将 MemoryMultipartFile 适配为 MultipartFile 接口
     */
    private static class AdaptedMultipartFile implements MultipartFile {
        private final MemoryMultipartFile memoryFile;

        public AdaptedMultipartFile(MemoryMultipartFile memoryFile) {
            this.memoryFile = memoryFile;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return memoryFile.getOriginalFilename();
        }

        @Override
        public String getContentType() {
            return memoryFile.getContentType();
        }

        @Override
        public boolean isEmpty() {
            return memoryFile.isEmpty();
        }

        @Override
        public long getSize() {
            return memoryFile.getSize();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return memoryFile.getContent();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return memoryFile.getInputStream();
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo not supported");
        }
    }

    @Override
    public byte[] downloadConversionResult(String taskId) throws Exception {
        TaskInfo taskInfo = taskManager.getTaskInfo(taskId);

        if (taskInfo == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        if (taskInfo.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("任务尚未完成: " + taskInfo.getStatus().getDescription());
        }

        ConversionResult result = taskInfo.getResult();
        byte[] content;

        // 根据任务类型获取文件内容
        if (taskInfo.getTaskType() == TaskType.SINGLE) {
            content = tempFileStorage.get(result.getPdfFileName());
            if (content == null) {
                // 如果临时存储已过期，从 R2 下载
                content = r2Service.downloadFile(result.getPdfFileName());
            }
        } else {
            content = tempFileStorage.get(result.getZipFileName());
            if (content == null) {
                // 如果临时存储已过期，从 R2 下载
                content = r2Service.downloadFile(result.getZipFileName());
            }
        }

        return content;
    }

    @Override
    public byte[] downloadFile(String fileName) throws Exception {
        // 先尝试从临时存储获取
        byte[] content = tempFileStorage.get(fileName);

        if (content == null) {
            // 临时存储中没有，从 R2 下载
            content = r2Service.downloadFile(fileName);
        }

        return content;
    }

    /**
     * 验证单个文件
     */
    private void validateFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("文件大小超过限制，最大支持 " + maxFileSize);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.toLowerCase().endsWith(".doc") &&
                        !originalFilename.toLowerCase().endsWith(".docx"))) {
            throw new IllegalArgumentException("只支持 .doc 和 .docx 格式的文件");
        }
    }

    /**
     * 验证多个文件
     */
    private void validateFiles(MultipartFile[] files) throws Exception {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (files.length > maxFileCount) {
            throw new IllegalArgumentException("最多支持 " + maxFileCount + " 个文件");
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }
    }
}
