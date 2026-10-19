FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /var/log/app

COPY target/order-service-debezium-*.jar app.jar

EXPOSE 8086

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
    CMD curl -sf http://localhost:8086/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
