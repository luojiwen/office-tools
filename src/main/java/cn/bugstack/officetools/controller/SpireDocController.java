package cn.bugstack.officetools.controller;

import cn.bugstack.officetools.domain.dto.ApiResponse;
import cn.bugstack.officetools.service.SpireDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Spire.Doc 文档处理控制器
 *
 * @author bugstack
 * @date 2026-01-13
 */
@RestController
@RequestMapping("/api/spire/doc")
public class SpireDocController {

    private final SpireDocService spireDocService;

    @Autowired
    public SpireDocController(SpireDocService spireDocService) {
        this.spireDocService = spireDocService;
    }

    /**
     * 转换 Word 文档格式
     *
     * @param file        上传的文件
     * @param targetFormat 目标格式 (pdf, html, txt, xps, etc.)
     * @return 转换后的文件或结果信息
     */
    @PostMapping("/convert")
    public ResponseEntity<?> convertDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String targetFormat) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "文件不能为空", null));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                    (!originalFilename.endsWith(".doc") && !originalFilename.endsWith(".docx"))) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "只支持 .doc 和 .docx 格式的文件", null));
            }

            ByteArrayOutputStream outputStream = spireDocService.convertDocument(
                    file.getInputStream(),
                    originalFilename,
                    targetFormat
            );

            String outputFilename = originalFilename.substring(0, originalFilename.lastIndexOf('.')) + "." + targetFormat;

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + outputFilename + "\"")
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "文档转换失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取文档信息
     *
     * @param file 上传的文件
     * @return 文档的详细信息
     */
    @PostMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentInfo(
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok()
                        .body(new ApiResponse<>(false, "文件不能为空", null));
            }

            Map<String, Object> documentInfo = spireDocService.getDocumentInfo(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, "获取文档信息成功", documentInfo));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取文档信息失败: " + e.getMessage(), null));
        }
    }

    /**
     * 提取文档中的文本内容
     *
     * @param file 上传的文件
     * @return 文档的文本内容
     */
    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<String>> extractText(
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok()
                        .body(new ApiResponse<>(false, "文件不能为空", null));
            }

            String textContent = spireDocService.extractText(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, "提取文本成功", textContent));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "提取文本失败: " + e.getMessage(), null));
        }
    }

    /**
     * 在文档中添加水印
     *
     * @param file     上传的文件
     * @param text     水印文本
     * @param isImage  是否为图片水印（默认为文本水印）
     * @return 添加水印后的文档
     */
    @PostMapping("/watermark")
    public ResponseEntity<?> addWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @RequestParam(value = "isImage", defaultValue = "false") boolean isImage) {

        try {
            ByteArrayOutputStream outputStream = spireDocService.addWatermark(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    text,
                    isImage
            );

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"watermarked_document.docx\"")
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "添加水印失败: " + e.getMessage(), null));
        }
    }

    /**
     * 加密文档
     *
     * @param file       上传的文件
     * @param password   加密密码
     * @param protectionType 保护类型 (all, readOnly, comments, etc.)
     * @return 加密后的文档
     */
    @PostMapping("/protect")
    public ResponseEntity<?> protectDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @RequestParam(value = "protectionType", defaultValue = "default") String protectionType) {

        try {
            ByteArrayOutputStream outputStream = spireDocService.protectDocument(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    password,
                    protectionType
            );

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"protected_document.docx\"")
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "文档加密失败: " + e.getMessage(), null));
        }
    }

    /**
     * 解密文档
     *
     * @param file     上传的文件
     * @param password 解密密码
     * @return 解密后的文档
     */
    @PostMapping("/unprotect")
    public ResponseEntity<?> unprotectDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {

        try {
            ByteArrayOutputStream outputStream = spireDocService.unprotectDocument(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    password
            );

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"unprotected_document.docx\"")
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "文档解密失败: " + e.getMessage(), null));
        }
    }
}
