package cn.bugstack.officetools.config;

import cn.bugstack.officetools.domain.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理客户端断开连接异常
     * 这通常发生在客户端超时或用户取消下载时，不需要向客户端返回错误响应
     */
    @ExceptionHandler({ClientAbortException.class, org.springframework.web.context.request.async.AsyncRequestNotUsableException.class})
    public void handleClientAbortException(Exception e) {
        // 只记录日志，不返回响应（连接已断开，无法写入）
        if (e instanceof ClientAbortException) {
            log.warn("客户端断开连接: {}", e.getMessage());
        } else {
            log.warn("异步请求不可用: {}", e.getMessage());
        }
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    /**
     * 处理文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("文件大小超限: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiResponse<>(false, "文件大小超过限制，最大支持 20MB", null));
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        log.warn("状态异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    /**
     * 处理存储空间不足异常
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException e) {
        log.warn("响应状态异常: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode())
                .body(new ApiResponse<>(false, e.getReason(), null));
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 检查是否是客户端断开连接的嵌套异常
        if (isClientAbortException(e)) {
            log.warn("检测到客户端断开连接: {}", e.getMessage());
            return null; // 不返回响应
        }

        log.error("未处理的异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "服务器内部错误: " + e.getMessage(), null));
    }

    /**
     * 检查异常是否由客户端断开连接引起
     */
    private boolean isClientAbortException(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ClientAbortException ||
                cause instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
