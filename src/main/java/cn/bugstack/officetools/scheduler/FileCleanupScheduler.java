package cn.bugstack.officetools.scheduler;

import cn.bugstack.officetools.domain.dto.TaskInfo;
import cn.bugstack.officetools.service.R2Service;
import cn.bugstack.officetools.service.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件清理定时任务
 */
@Slf4j
@Component
public class FileCleanupScheduler {

    @Autowired
    private R2Service r2Service;

    @Autowired
    private TaskManager taskManager;

    @Value("${conversion.file.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${conversion.file.cleanup.expire-hours:1}")
    private int expireHours;

    /**
     * 定时清理过期文件
     * 每小时执行一次
     */
    @Scheduled(cron = "${conversion.file.cleanup.cron:0 0 * * * ?}")
    public void cleanupExpiredFiles() {
        if (!cleanupEnabled) {
            return;
        }

        log.info("开始清理过期文件，过期时间: {} 小时", expireHours);

        try {
            // 清理过期的转换文件
            cleanupExpiredConvertedFiles();

            // 清理过期的任务记录
            taskManager.cleanupExpiredTasks(expireHours * 60);

            log.info("文件清理完成");

        } catch (Exception e) {
            log.error("文件清理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理过期的转换文件
     */
    private void cleanupExpiredConvertedFiles() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(expireHours);
        int deletedCount = 0;

        // 获取所有任务
        List<TaskInfo> tasks = taskManager.getAllTasks();

        for (TaskInfo task : tasks) {
            if (task.getCompletedTime() != null &&
                    task.getCompletedTime().isBefore(expireTime)) {

                // 删除 R2 中的文件
                if (task.getResult() != null) {
                    try {
                        if (task.getResult().getPdfFileName() != null) {
                            r2Service.deleteFile(task.getResult().getPdfFileName());
                            deletedCount++;
                            log.debug("删除 PDF 文件: {}", task.getResult().getPdfFileName());
                        }
                        if (task.getResult().getZipFileName() != null) {
                            r2Service.deleteFile(task.getResult().getZipFileName());
                            deletedCount++;
                            log.debug("删除 ZIP 文件: {}", task.getResult().getZipFileName());
                        }
                    } catch (Exception e) {
                        log.warn("删除文件失败: {}", e.getMessage());
                    }
                }
            }
        }

        if (deletedCount > 0) {
            log.info("删除过期文件: {} 个", deletedCount);
        }
    }
}
