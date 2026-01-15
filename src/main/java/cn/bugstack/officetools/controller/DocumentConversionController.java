package cn.bugstack.officetools.controller;

import cn.bugstack.officetools.domain.dto.ApiResponse;
import cn.bugstack.officetools.domain.dto.ConversionResult;
import cn.bugstack.officetools.domain.dto.TaskInfo;
import cn.bugstack.officetools.service.DocumentConversionService;
import cn.bugstack.officetools.service.TaskManager;
import cn.bugstack.officetools.util.HttpHeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档转换控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/convert")
public class DocumentConversionController {

    @Autowired
    private DocumentConversionService conversionService;

    @Autowired
    private TaskManager taskManager;

    /**
     * 同步转换 - 单文件
     */
    @PostMapping("/word-to-pdf/sync/single")
    public ResponseEntity<?> convertSingleSync(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "returnFile", defaultValue = "true") boolean returnFile) {

        try {
            ConversionResult result = conversionService.convertSingleSync(file, returnFile);

            // 如果需要返回文件，直接返回文件流
            if (returnFile && result.isSuccess()) {
                byte[] content = conversionService.downloadFile(result.getPdfFileName());
                return ResponseEntity.ok()
                        .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue(result.getPdfFileName()))
                        .header("Content-Type", "application/pdf")
                        .body(content);
            }

            // 否则返回 JSON 结果
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, result.getMessage(), result));

        } catch (Exception e) {
            log.error("单文件转换失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "转换失败: " + e.getMessage(), null));
        }
    }

    /**
     * 同步转换 - 批量
     */
    @PostMapping("/word-to-pdf/sync/batch")
    public ResponseEntity<?> convertBatchSync(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "returnFile", defaultValue = "true") boolean returnFile) {

        try {
            ConversionResult result = conversionService.convertBatchSync(files, returnFile);

            // 如果需要返回文件，直接返回 ZIP 文件流
            if (returnFile && result.isSuccess() && result.getZipFileName() != null) {
                byte[] content = conversionService.downloadFile(result.getZipFileName());
                return ResponseEntity.ok()
                        .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue(result.getZipFileName()))
                        .header("Content-Type", "application/zip")
                        .body(content);
            }

            // 否则返回 JSON 结果
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, result.getMessage(), result));

        } catch (Exception e) {
            log.error("批量转换失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "转换失败: " + e.getMessage(), null));
        }
    }

    /**
     * 异步转换 - 单文件
     */
    @PostMapping("/word-to-pdf/async/single")
    public ResponseEntity<ApiResponse<Map<String, Object>>> convertSingleAsync(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "callbackUrl", required = false) String callbackUrl) {

        try {
            String taskId = conversionService.convertSingleAsync(file, callbackUrl);

            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("status", "PENDING");
            data.put("estimatedTime", 30);
            data.put("statusUrl", "/api/convert/task/" + taskId);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, "任务已提交", data));

        } catch (Exception e) {
            log.error("提交单文件异步任务失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "提交任务失败: " + e.getMessage(), null));
        }
    }

    /**
     * 异步转换 - 批量
     */
    @PostMapping("/word-to-pdf/async/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> convertBatchAsync(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "callbackUrl", required = false) String callbackUrl) {

        try {
            String taskId = conversionService.convertBatchAsync(files, callbackUrl);

            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("status", "PENDING");
            data.put("totalFiles", files.length);
            data.put("estimatedTime", files.length * 30);
            data.put("statusUrl", "/api/convert/task/" + taskId);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, "批量任务已提交", data));

        } catch (Exception e) {
            log.error("提交批量异步任务失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "提交任务失败: " + e.getMessage(), null));
        }
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<TaskInfo>> getTaskStatus(@PathVariable String taskId) {
        TaskInfo taskInfo = taskManager.getTaskInfo(taskId);

        if (taskInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "任务不存在", null));
        }

        return ResponseEntity.ok()
                .body(new ApiResponse<>(true, "查询成功", taskInfo));
    }

    /**
     * 下载转换结果
     */
    @GetMapping("/download/{taskId}")
    public ResponseEntity<?> downloadResult(@PathVariable String taskId) {
        try {
            TaskInfo taskInfo = taskManager.getTaskInfo(taskId);

            if (taskInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "任务不存在", null));
            }

            if (taskInfo.getStatus() != cn.bugstack.officetools.domain.dto.TaskStatus.COMPLETED) {
                Map<String, Object> data = new HashMap<>();
                data.put("status", taskInfo.getStatus().name());
                data.put("progress", taskInfo.getProgress());

                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ApiResponse<>(false, "任务尚未完成", data));
            }

            ConversionResult result = taskInfo.getResult();
            byte[] content = conversionService.downloadConversionResult(taskId);

            String fileName;
            String contentType;
            if (taskInfo.getTaskType() == cn.bugstack.officetools.domain.dto.TaskType.SINGLE) {
                fileName = result.getPdfFileName();
                contentType = "application/pdf";
            } else {
                fileName = result.getZipFileName();
                contentType = "application/zip";
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue(fileName))
                    .header("Content-Type", contentType)
                    .body(content);

        } catch (Exception e) {
            log.error("下载失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "下载失败: " + e.getMessage(), null));
        }
    }
}
