FROM csanchez/maven:3-azulzulu-23-alpine AS builder

COPY . /komet/

WORKDIR /komet/

RUN ls /komet/
RUN ./mvnw clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true
RUN ./mvnw clean -f application -Pjpro jpro:release

FROM azul/zulu-openjdk-alpine:23-latest

# Update the package list and install bash (for script compatibility)
RUN apk update && apk add --no-cache --upgrade bash

# Install the glibc compatibility library and GTK+ 3.0 required for JPro applications
RUN apk add --no-cache libc6-compat gtk+3.0

# Copy the JPro application to the image
COPY --from=builder /komet/application/target/komet-jpro.zip /komet-jpro.zip

# Unzip the JPro application
RUN unzip /komet-jpro.zip -d /jproserver/

WORKDIR /jproserver/komet-jpro/

# Start the JPro server
CMD (cd jproserver/komet-jpro/; ./bin/restart.sh)