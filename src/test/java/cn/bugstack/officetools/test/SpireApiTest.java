package cn.bugstack.officetools.test;

import com.spire.doc.Document;
import java.lang.reflect.Method;

/**
 * Spire.Doc API 测试类 - 用于检查可用的方法
 */
public class SpireApiTest {
    public static void main(String[] args) {
        System.out.println("=== Document 类可用方法 ===");
        Method[] methods = Document.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.contains("load") || name.contains("save") ||
                name.contains("protect") || name.contains("watermark")) {
                System.out.println(name + " - " + method.getParameterCount());
            }
        }
    }
}
