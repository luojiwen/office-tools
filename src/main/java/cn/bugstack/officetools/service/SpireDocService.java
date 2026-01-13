package cn.bugstack.officetools.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Spire.Doc 文档处理服务接口
 *
 * @author bugstack
 * @date 2026-01-13
 */
public interface SpireDocService {

    /**
     * 转换 Word 文档格式
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param targetFormat       目标格式 (pdf, html, txt, xps, etc.)
     * @return 转换后的文档输出流
     * @throws Exception 转换失败时抛出异常
     */
    ByteArrayOutputStream convertDocument(InputStream inputStream, String originalFilename, String targetFormat) throws Exception;

    /**
     * 获取文档信息
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @return 包含文档详细信息的 Map
     * @throws Exception 获取失败时抛出异常
     */
    Map<String, Object> getDocumentInfo(InputStream inputStream, String originalFilename) throws Exception;

    /**
     * 提取文档中的文本内容
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @return 文档的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    String extractText(InputStream inputStream, String originalFilename) throws Exception;

    /**
     * 在文档中添加水印
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param watermarkText      水印文本
     * @param isImageWatermark   是否为图片水印
     * @return 添加水印后的文档输出流
     * @throws Exception 添加失败时抛出异常
     */
    ByteArrayOutputStream addWatermark(InputStream inputStream, String originalFilename, String watermarkText, boolean isImageWatermark) throws Exception;

    /**
     * 加密文档
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param password           加密密码
     * @param protectionType     保护类型
     * @return 加密后的文档输出流
     * @throws Exception 加密失败时抛出异常
     */
    ByteArrayOutputStream protectDocument(InputStream inputStream, String originalFilename, String password, String protectionType) throws Exception;

    /**
     * 解密文档
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param password           解密密码
     * @return 解密后的文档输出流
     * @throws Exception 解密失败时抛出异常
     */
    ByteArrayOutputStream unprotectDocument(InputStream inputStream, String originalFilename, String password) throws Exception;
}
