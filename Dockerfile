# syntax=docker/dockerfile:1.7

# ---- Stage 1: Build ----
FROM maven:3.9.11-sapmachine-25 AS build
# The upstream maven image ships `ENV MAVEN_CONFIG=/root/.m2` so its entrypoint
# script can point Maven at the mounted local repo. During `docker build`,
# `RUN` skips the entrypoint, so that variable leaks into the shell unchanged.
# The `mvnw` script then splices `$MAVEN_CONFIG` straight into Maven's CLI
# args (see `MAVEN_CMD_LINE_ARGS="$MAVEN_CONFIG $@"` near the bottom of mvnw),
# causing Maven to interpret `/root/.m2` as a lifecycle phase and fail with
# `Unknown lifecycle phase "/root/.m2"`. Clearing it for the build stage keeps
# the wrapper behaviour sane while the BuildKit cache mount below still
# provides the `/root/.m2` directory at build time.
ENV MAVEN_CONFIG=""
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn ./.mvn
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp -DskipTests dependency:go-offline
COPY src ./src
COPY checkstyle.xml eclipse-formatter-profile.xml eclipse.importorder license-header ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp -DskipTests package \
    && mkdir -p /workspace/dependency \
    && (cd /workspace/dependency; jar -xf /workspace/target/backend-*.jar)

# ---- Stage 2: Runtime ----
FROM sapmachine:26-jre-headless-ubuntu-noble
LABEL org.opencontainers.image.source="https://github.com/x-plaza/xplaza-backend"
LABEL org.opencontainers.image.description="X-Plaza e-commerce backend"
LABEL org.opencontainers.image.licenses="Proprietary"

# Ubuntu Noble ships with a default `ubuntu` user at UID/GID 1000, which
# collides with `groupadd --gid 1000` and fails the build with
# `groupadd: GID '1000' already exists` (apt exit code 4). We drop that
# default account and claim UID/GID 10001 for the service user — still a
# non-privileged, non-login account, just well above the typical host-user
# range so a bind-mount from the host won't silently map to `xplaza`.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && (userdel --remove ubuntu 2>/dev/null || true) \
    && (getent group ubuntu >/dev/null && groupdel ubuntu || true) \
    && groupadd --system --gid 10001 xplaza \
    && useradd --system --uid 10001 --gid xplaza --no-create-home --shell /usr/sbin/nologin xplaza

USER xplaza:xplaza
WORKDIR /app
ARG DEPENDENCY=/workspace/dependency
COPY --from=build --chown=xplaza:xplaza ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build --chown=xplaza:xplaza ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build --chown=xplaza:xplaza ${DEPENDENCY}/BOOT-INF/classes /app

ENV JAVA_OPTS="--enable-preview -XX:+UseZGC -XX:+ZGenerational -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=cloud
ENV PORT=10001

EXPOSE 10001

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl --silent --fail http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -cp /app:/app/lib/* com.xplaza.backend.XplazaApplication"]
