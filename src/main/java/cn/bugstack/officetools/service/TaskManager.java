package cn.bugstack.officetools.service;

import cn.bugstack.officetools.domain.dto.ConversionResult;
import cn.bugstack.officetools.domain.dto.TaskInfo;
import cn.bugstack.officetools.domain.dto.TaskStatus;
import cn.bugstack.officetools.domain.dto.TaskType;

import java.util.List;

/**
 * 任务管理接口
 */
public interface TaskManager {

    /**
     * 创建新任务
     *
     * @param taskType   任务类型
     * @param totalFiles 总文件数
     * @return 任务 ID
     */
    String createTask(TaskType taskType, int totalFiles);

    /**
     * 更新任务状态
     *
     * @param taskId  任务 ID
     * @param status  新状态
     * @param progress 进度 (0-100)
     */
    void updateTaskStatus(String taskId, TaskStatus status, int progress);

    /**
     * 设置任务结果
     *
     * @param taskId 任务 ID
     * @param result 转换结果
     */
    void setTaskResult(String taskId, ConversionResult result);

    /**
     * 设置任务错误
     *
     * @param taskId 任务 ID
     * @param error  错误信息
     */
    void setTaskError(String taskId, String error);

    /**
     * 获取任务信息
     *
     * @param taskId 任务 ID
     * @return 任务信息
     */
    TaskInfo getTaskInfo(String taskId);

    /**
     * 获取所有任务
     *
     * @return 任务列表
     */
    List<TaskInfo> getAllTasks();

    /**
     * 清理过期任务
     *
     * @param expireMinutes 过期时间(分钟)
     */
    void cleanupExpiredTasks(int expireMinutes);
}
