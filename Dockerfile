FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY . /workspace/app
RUN --mount=type=cache,target=/root/.gradle ls && ./gradlew clean build copyAgentJars
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG AGENTS=/workspace/app/build/agents
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${AGENTS} /agents
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT [ \
    "java","-cp","app:app/lib/*",\
    "-javaagent:/agents/aspectjweaver.jar",\
    "-javaagent:/agents/spring-instrument.jar",\
    "--add-opens=java.base/java.lang=ALL-UNNAMED",\
    "com.consartist.spring.cache.Application"]