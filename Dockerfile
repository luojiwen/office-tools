# Office Tools 多阶段构建 Dockerfile
# 针对 Render.com 部署优化

# 阶段 1: 构建阶段
FROM maven:3.9.12-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 先复制 Maven wrapper 和 pom.xml (为了更好的缓存效果)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 设置 Maven wrapper 可执行权限
RUN chmod +x mvnw

# 下载依赖 (当 pom.xml 不变时会使用缓存)
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
# 跳过测试以加快生产环境构建速度
RUN ./mvnw clean package -DskipTests -B

# 阶段 2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

# 安装 dumb-init 用于正确的信号处理
RUN apk add --no-cache dumb-init curl

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
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 运行应用
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
