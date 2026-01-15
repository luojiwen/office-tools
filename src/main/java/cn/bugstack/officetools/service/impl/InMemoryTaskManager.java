package cn.bugstack.officetools.service.impl;

import cn.bugstack.officetools.domain.dto.ConversionResult;
import cn.bugstack.officetools.domain.dto.TaskInfo;
import cn.bugstack.officetools.domain.dto.TaskStatus;
import cn.bugstack.officetools.domain.dto.TaskType;
import cn.bugstack.officetools.service.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存任务管理实现
 */
@Slf4j
@Service
public class InMemoryTaskManager implements TaskManager {

    private final ConcurrentHashMap<String, TaskInfo> tasks = new ConcurrentHashMap<>();

    @Override
    public String createTask(TaskType taskType, int totalFiles) {
        String taskId = UUID.randomUUID().toString();

        TaskInfo taskInfo = TaskInfo.builder()
                .taskId(taskId)
                .taskType(taskType)
                .status(TaskStatus.PENDING)
                .progress(0)
                .totalFiles(totalFiles)
                .processedFiles(0)
                .createdTime(LocalDateTime.now())
                .build();

        tasks.put(taskId, taskInfo);
        log.debug("创建任务: {}, 类型: {}, 文件数: {}", taskId, taskType, totalFiles);

        return taskId;
    }

    @Override
    public void updateTaskStatus(String taskId, TaskStatus status, int progress) {
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo != null) {
            taskInfo.setStatus(status);
            taskInfo.setProgress(progress);

            if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
                taskInfo.setCompletedTime(LocalDateTime.now());
            }

            log.debug("更新任务状态: {}, 状态: {}, 进度: {}%", taskId, status, progress);
        }
    }

    @Override
    public void setTaskResult(String taskId, ConversionResult result) {
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo != null) {
            taskInfo.setResult(result);
            taskInfo.setProcessedFiles(result.getTotalFiles() != null ? result.getTotalFiles() : 1);
            log.debug("设置任务结果: {}", taskId);
        }
    }

    @Override
    public void setTaskError(String taskId, String error) {
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo != null) {
            taskInfo.setError(error);
            log.debug("设置任务错误: {}, 错误: {}", taskId, error);
        }
    }

    @Override
    public TaskInfo getTaskInfo(String taskId) {
        return tasks.get(taskId);
    }

    @Override
    public List<TaskInfo> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * 定时清理过期任务 (每小时执行一次)
     */
    @Scheduled(fixedRate = 3600000)
    public void scheduledCleanupExpiredTasks() {
        cleanupExpiredTasks(60); // 清理 60 分钟前的任务
    }

    /**
     * 清理过期任务
     *
     * @param expireMinutes 过期时间(分钟)
     */
    public void cleanupExpiredTasks(int expireMinutes) {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(expireMinutes);
        int removedCount = 0;

        for (TaskInfo taskInfo : tasks.values()) {
            LocalDateTime taskTime = taskInfo.getCompletedTime() != null ?
                    taskInfo.getCompletedTime() : taskInfo.getCreatedTime();

            if (taskTime.isBefore(expireTime)) {
                tasks.remove(taskInfo.getTaskId());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("清理过期任务: {} 个", removedCount);
        }
    }
}
