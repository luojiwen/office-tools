package cn.bugstack.officetools.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Aspose.Words 文档处理服务接口
 *
 * @author bugstack
 * @date 2026-01-13
 */
public interface AsposeWordService {

    /**
     * 转换 Word 文档格式
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param targetFormat       目标格式 (pdf, html, txt, docx, doc, etc.)
     * @return 转换后的文档输出流
     * @throws Exception 转换失败时抛出异常
     */
    ByteArrayOutputStream convertDocument(InputStream inputStream, String originalFilename, String targetFormat) throws Exception;

    /**
     * 获取文档信息
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @return 包含文档详细信息的 Map (页数、字数、段落数、作者等)
     * @throws Exception 获取失败时抛出异常
     */
    Map<String, Object> getDocumentInfo(InputStream inputStream, String originalFilename) throws Exception;

    /**
     * 合并多个 Word 文档
     *
     * @param files 要合并的文件数组
     * @return 合并后的文档输出流
     * @throws Exception 合并失败时抛出异常
     */
    ByteArrayOutputStream mergeDocuments(MultipartFile[] files) throws Exception;

    /**
     * 在文档中插入文本
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param text               要插入的文本
     * @param position           插入位置 (start, end)
     * @return 修改后的文档输出流
     * @throws Exception 插入失败时抛出异常
     */
    ByteArrayOutputStream insertText(InputStream inputStream, String originalFilename, String text, String position) throws Exception;

    /**
     * 替换文档中的文本
     *
     * @param inputStream        文档输入流
     * @param originalFilename   原始文件名
     * @param oldText            要替换的旧文本
     * @param newText            替换的新文本
     * @param matchCase          是否区分大小写
     * @return 修改后的文档输出流
     * @throws Exception 替换失败时抛出异常
     */
    ByteArrayOutputStream replaceText(InputStream inputStream, String originalFilename, String oldText, String newText, boolean matchCase) throws Exception;
}
