FROM eclipse-temurin:25-jdk-alpine

WORKDIR /app

# Build argument for version
ARG APP_VERSION=1.0.0

# Copy application artifact
COPY target/product-service-${APP_VERSION}.jar app.jar

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher \
    -jar app.jar health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
