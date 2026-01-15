package cn.bugstack.officetools.controller;

import cn.bugstack.officetools.service.R2Service;
import cn.bugstack.officetools.domain.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloudflare R2 文件上传控制器
 * 提供文件上传、删除等 REST API
 */
@RestController
@RequestMapping("/api/r2")
@CrossOrigin(origins = "*")
public class R2Controller {

    @Autowired
    private R2Service r2Service;

    /**
     * 上传单个文件
     *
     * @param file 要上传的文件
     * @param fileName 可选的目标文件名
     * @return 上传结果，包含文件访问 URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName) {

        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "文件不能为空", null));
            }

            // 上传文件
            String fileUrl = r2Service.uploadFile(file, fileName);

            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName != null ? fileName : extractFileNameFromUrl(fileUrl));
            result.put("url", fileUrl);
            result.put("size", String.valueOf(file.getSize()));
            result.put("contentType", file.getContentType());

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, "上传成功", result));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "上传失败: " + e.getMessage(), null));
        }
    }

    /**
     * 删除文件
     *
     * @param fileName 要删除的文件名
     * @return 删除结果
     */
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileName) {
        try {
            r2Service.deleteFile(fileName);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, "删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "删除失败: " + e.getMessage(), null));
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileName 文件名
     * @return 检查结果
     */
    @GetMapping("/exists/{fileName}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFileExists(@PathVariable String fileName) {
        boolean exists = r2Service.fileExists(fileName);

        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);

        return ResponseEntity.ok()
                .body(new ApiResponse<>(true, "检查完成", result));
    }

    /**
     * 从 URL 中提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}