package cn.bugstack.officetools.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 头部工具类
 *
 * @author bugstack
 * @date 2026-01-14
 */
public class HttpHeaderUtil {

    /**
     * 创建 Content-Disposition 头部的值
     * 支持包含中文等非 ASCII 字符的文件名
     * 使用 RFC 5987 标准编码
     *
     * @param filename 文件名
     * @return Content-Disposition 头部值
     */
    public static String createContentDispositionValue(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "attachment";
        }

        // 检查文件名是否包含非 ASCII 字符
        if (isAscii(filename)) {
            // 纯 ASCII 文件名，直接使用
            return "attachment; filename=\"" + filename + "\"";
        }

        // 包含非 ASCII 字符（如中文），使用 RFC 5987 编码
        String encodedFilename = encodeFilename(filename);

        // 同时提供两个参数：
        // - filename: URL编码的文件名（兼容旧客户端）
        // - filename*: RFC 5987 标准编码（现代客户端优先使用）
        return "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    /**
     * 检查字符串是否仅包含 ASCII 字符
     *
     * @param str 输入字符串
     * @return 如果仅包含 ASCII 字符返回 true，否则返回 false
     */
    private static boolean isAscii(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用 URL 编码对文件名进行编码（RFC 5987 格式）
     *
     * @param filename 原始文件名
     * @return URL 编码后的文件名
     */
    private static String encodeFilename(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
    }
}