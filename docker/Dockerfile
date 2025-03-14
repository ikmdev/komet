FROM azul/zulu-openjdk-alpine:23-latest

# Update the package list and install bash (for script compatibility)
RUN apk update && apk add --no-cache --upgrade bash

# Install the glibc compatibility library and GTK+ 3.0 required for JPro applications
RUN apk add --no-cache libc6-compat gtk+3.0

# Copy the JPro distribution to the image
ADD . jproserver/

# Start the JPro server
CMD (cd jproserver/; ./bin/restart.sh)