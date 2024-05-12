# OLD WAY: https://spring.io/guides/topicals/spring-boot-docker
#  This way gave me a lot of trouble with AspectJ, so I switched to the...
# NEW WAY: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#container-images
#  This dockerfile uses stuff from both ways, actually.
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY . /workspace/app
RUN --mount=type=cache,target=/root/.gradle ls && ./gradlew clean build -DskipTests
RUN cp build/libs/*-SNAPSHOT.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG APP=/workspace/app
RUN mkdir agents
COPY --from=build ${APP}/dependencies ./
COPY --from=build ${APP}/spring-boot-loader/ ./
RUN cp BOOT-INF/lib/aspectjweaver-*.jar agents/aspectjweaver.jar
RUN cp BOOT-INF/lib/spring-instrument-*.jar agents/spring-instrument.jar
COPY --from=build ${APP}/snapshot-dependencies/ ./
COPY --from=build ${APP}/application/ ./
ENTRYPOINT [\
   "java", \
    "-javaagent:/agents/aspectjweaver.jar",\
    "-javaagent:/agents/spring-instrument.jar",\
    "--add-opens=java.base/java.lang=ALL-UNNAMED",\
   "org.springframework.boot.loader.launch.JarLauncher"\
]