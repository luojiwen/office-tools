package cn.bugstack.officetools.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo {
    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务类型
     */
    private TaskType taskType;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 进度 (0-100)
     */
    private int progress;

    /**
     * 总文件数
     */
    private int totalFiles;

    /**
     * 已处理文件数
     */
    private int processedFiles;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 转换结果
     */
    private ConversionResult result;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 获取已用时间（秒）
     */
    public long getElapsedTime() {
        LocalDateTime end = completedTime != null ? completedTime : LocalDateTime.now();
        return java.time.temporal.ChronoUnit.SECONDS.between(createdTime, end);
    }
}
