# Office Tools 多阶段构建 Dockerfile
# 针对 Render.com 部署优化

# 阶段 1: 构建阶段
FROM maven:3.9.12-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 先复制 Maven wrapper 和 pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 设置 Maven wrapper 可执行权限
RUN chmod +x mvnw

# 复制 system-scoped 依赖 JAR 文件
# 必须在 dependency:go-offline 之前复制，否则 Maven 无法解析 system-scoped 依赖
COPY src/main/resources/lib ./src/main/resources/lib

# 下载其他依赖 (system-scoped 依赖已经在本地了)
RUN ./mvnw dependency:go-offline -B || true

# 复制源代码
COPY src ./src

# 构建应用
# 跳过测试以加快生产环境构建速度
RUN ./mvnw clean package -DskipTests -B

# 阶段 2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

# 安装必要的包和中文字体
# - fontconfig: 字体配置工具
# - font-noto-cjk: Google Noto CJK 字体（支持中文、日文、韩文）
# - dumb-init: 正确的信号处理
# - curl: 健康检查
RUN apk add --no-cache \
    fontconfig \
    font-noto-cjk \
    dumb-init \
    curl && \
    # 更新字体缓存
    fc-cache -fv

# 创建应用用户和组
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 从构建阶段复制已构建的 JAR 文件
COPY --from=builder /app/target/office-tools-*.jar app.jar

# 将所有权更改为应用用户
RUN chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 暴露应用端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 设置生产环境 JVM 参数
# 针对 Render 免费版 512MB 内存限制优化
# -Xmx240m: 最大堆内存 240MB（减少堆内存，给元空间更多空间）
# -Xms100m: 初始堆内存 100MB（动态增长，节省内存）
# -XX:MaxMetaspaceSize=128m: 增加元空间到 128MB（Aspose.Words 需要大量类加载）
# -XX:CompressedClassSpaceSize=64m: 压缩类空间大小
# -XX:+UseG1GC: 使用 G1 垃圾收集器（低延迟）
# -XX:+UseStringDeduplication: 字符串去重，减少内存占用
# -XX:MaxGCPauseMillis=200: 设置最大 GC 暂停时间
# -XX:InitiatingHeapOccupancyPercent=35: 提前触发 GC，避免内存峰值
# -Djava.awt.headless=true: 无头模式（服务器无 GUI）
#
# 内存预估: 240MB(堆) + 128MB(元空间) + 64MB(压缩类空间) + 50MB(JVM/OS) = 482MB < 512MB ✅
ENV JAVA_OPTS="-Xmx240m -Xms100m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=64m \
    -XX:+UseG1GC -XX:+UseStringDeduplication \
    -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Dlogging.file.name=/app/logs/application.log"

# 运行应用
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
