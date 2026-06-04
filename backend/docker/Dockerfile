# =============================================
# 网上书店后端微服务 - 多阶段构建Dockerfile
# 阶段1: Maven编译 (JDK 17)
# 阶段2: JRE运行时
# =============================================

FROM maven:3.9-eclipse-temurin-17-alpine AS builder

ARG SERVICE_NAME
WORKDIR /build

# 配置阿里云Maven镜像加速
RUN mkdir -p /root/.m2 && echo '<?xml version="1.0" encoding="UTF-8"?>\
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"\
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\
  <mirrors>\
    <mirror>\
      <id>aliyun-maven</id>\
      <mirrorOf>central</mirrorOf>\
      <name>Aliyun Maven</name>\
      <url>https://maven.aliyun.com/repository/public</url>\
    </mirror>\
  </mirrors>\
</settings>' > /root/.m2/settings.xml

# 先复制父POM和所有模块的POM（利用Docker缓存层）
COPY pom.xml .
COPY bookstore-common/pom.xml bookstore-common/
COPY bookstore-gateway/pom.xml bookstore-gateway/
COPY bookstore-auth-service/pom.xml bookstore-auth-service/
COPY bookstore-user-service/pom.xml bookstore-user-service/
COPY bookstore-product-service/pom.xml bookstore-product-service/
COPY bookstore-order-service/pom.xml bookstore-order-service/

# 下载依赖（利用缓存，改动源码不会重新下载）
RUN mvn dependency:go-offline -pl ${SERVICE_NAME} -am -DskipTests || true

# 复制源码
COPY bookstore-common/src bookstore-common/src/
COPY bookstore-gateway/src bookstore-gateway/src/
COPY bookstore-auth-service/src bookstore-auth-service/src/
COPY bookstore-user-service/src bookstore-user-service/src/
COPY bookstore-product-service/src bookstore-product-service/src/
COPY bookstore-order-service/src bookstore-order-service/src/

# 编译指定模块（跳过测试加速构建）
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# =============================================
# 运行时阶段
# =============================================
FROM eclipse-temurin:17-jre-alpine

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# 从构建阶段复制JAR包
COPY --from=builder /build/${SERVICE_NAME}/target/${SERVICE_NAME}-1.0.0.jar app.jar

# 创建非root用户运行（安全最佳实践）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
