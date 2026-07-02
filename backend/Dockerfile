FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .

COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
ENV APP_STORAGE_UPLOAD_DIR=/app/data/uploads

RUN apt-get update \
	&& apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng \
	&& rm -rf /var/lib/apt/lists/* \
	&& mkdir -p /app/data/uploads
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
