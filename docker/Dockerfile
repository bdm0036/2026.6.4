# 后端微服务通用Dockerfile
FROM eclipse-temurin:17-jre-alpine

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# 复制对应服务的jar包
COPY ${SERVICE_NAME}/target/${SERVICE_NAME}-1.0.0.jar app.jar

EXPOSE 8080
EXPOSE 8081
EXPOSE 8082
EXPOSE 8083
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
