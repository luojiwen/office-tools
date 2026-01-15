package cn.bugstack.officetools.controller;

import cn.bugstack.officetools.domain.dto.ApiResponse;
import cn.bugstack.officetools.service.AsposeWordService;
import cn.bugstack.officetools.util.HttpHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Aspose.Words 文档处理控制器
 *
 * @author bugstack
 * @date 2026-01-13
 */
@RestController
@RequestMapping("/api/aspose/word")
public class AsposeWordController {

    private final AsposeWordService asposeWordService;

    @Autowired
    public AsposeWordController(AsposeWordService asposeWordService) {
        this.asposeWordService = asposeWordService;
    }

    /**
     * 转换 Word 文档格式
     *
     * @param file        上传的文件
     * @param targetFormat 目标格式 (pdf, html, txt, etc.)
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

            ByteArrayOutputStream outputStream = asposeWordService.convertDocument(
                    file.getInputStream(),
                    originalFilename,
                    targetFormat
            );

            // 生成带时间戳的输出文件名：原文件名_yyyy-MM-dd-HH-mm-ss.扩展名
            String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String outputFilename = baseName + "_" + timestamp + "." + targetFormat;

            return ResponseEntity.ok()
                    .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue(outputFilename))
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

            Map<String, Object> documentInfo = asposeWordService.getDocumentInfo(
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
     * 合并多个 Word 文档
     *
     * @param files 要合并的文件列表
     * @return 合并后的文档
     */
    @PostMapping("/merge")
    public ResponseEntity<?> mergeDocuments(@RequestParam("files") MultipartFile[] files) {

        try {
            if (files == null || files.length < 2) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "至少需要2个文件才能进行合并", null));
            }

            ByteArrayOutputStream outputStream = asposeWordService.mergeDocuments(files);

            return ResponseEntity.ok()
                    .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue("merged_document.docx"))
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "文档合并失败: " + e.getMessage(), null));
        }
    }

    /**
     * 在文档中插入文本
     *
     * @param file      上传的文件
     * @param text      要插入的文本
     * @param position  插入位置 (start, end)
     * @return 修改后的文档
     */
    @PostMapping("/insert")
    public ResponseEntity<?> insertText(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @RequestParam(value = "position", defaultValue = "end") String position) {

        try {
            ByteArrayOutputStream outputStream = asposeWordService.insertText(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    text,
                    position
            );

            return ResponseEntity.ok()
                    .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue("modified_document.docx"))
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "插入文本失败: " + e.getMessage(), null));
        }
    }

    /**
     * 替换文档中的文本
     *
     * @param file        上传的文件
     * @param oldText     要替换的旧文本
     * @param newText     替换的新文本
     * @param matchCase   是否区分大小写
     * @return 修改后的文档
     */
    @PostMapping("/replace")
    public ResponseEntity<?> replaceText(
            @RequestParam("file") MultipartFile file,
            @RequestParam("oldText") String oldText,
            @RequestParam("newText") String newText,
            @RequestParam(value = "matchCase", defaultValue = "false") boolean matchCase) {

        try {
            ByteArrayOutputStream outputStream = asposeWordService.replaceText(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    oldText,
                    newText,
                    matchCase
            );

            return ResponseEntity.ok()
                    .header("Content-Disposition", HttpHeaderUtil.createContentDispositionValue("modified_document.docx"))
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "替换文本失败: " + e.getMessage(), null));
        }
    }
}