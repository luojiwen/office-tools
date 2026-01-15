package cn.bugstack.officetools.service;

import cn.bugstack.officetools.domain.dto.ConversionResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档转换服务接口
 */
public interface DocumentConversionService {

    /**
     * 同步转换单个 Word 文件为 PDF
     *
     * @param file       Word 文件
     * @param returnFile 是否直接返回文件流
     * @return 转换结果
     * @throws Exception 转换失败时抛出异常
     */
    ConversionResult convertSingleSync(MultipartFile file, boolean returnFile) throws Exception;

    /**
     * 同步批量转换 Word 文件为 PDF
     *
     * @param files      Word 文件数组
     * @param returnFile 是否直接返回 ZIP 文件流
     * @return 转换结果
     * @throws Exception 转换失败时抛出异常
     */
    ConversionResult convertBatchSync(MultipartFile[] files, boolean returnFile) throws Exception;

    /**
     * 异步转换单个 Word 文件为 PDF
     *
     * @param file        Word 文件
     * @param callbackUrl 完成后回调地址
     * @return 任务 ID
     */
    String convertSingleAsync(MultipartFile file, String callbackUrl);

    /**
     * 异步批量转换 Word 文件为 PDF
     *
     * @param files       Word 文件数组
     * @param callbackUrl 完成后回调地址
     * @return 任务 ID
     */
    String convertBatchAsync(MultipartFile[] files, String callbackUrl);

    /**
     * 下载转换结果（通过任务 ID）
     *
     * @param taskId 任务 ID
     * @return 文件内容
     * @throws Exception 下载失败时抛出异常
     */
    byte[] downloadConversionResult(String taskId) throws Exception;

    /**
     * 下载文件（通过文件名）
     *
     * @param fileName 文件名
     * @return 文件内容
     * @throws Exception 下载失败时抛出异常
     */
    byte[] downloadFile(String fileName) throws Exception;
}
