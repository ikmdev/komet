FROM ubuntu:24.04

# Update the package list
RUN apt update

# Install xorg and libgtk-3-0 needed to run JPro applications
RUN apt install -y xorg libgtk-3-0

# Ensure the necessary packages are present
RUN apt install -y wget software-properties-common

# Import Azul’s public key:
RUN apt install -y gnupg ca-certificates curl && \
    curl -s https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" | tee /etc/apt/sources.list.d/zulu.list

# Install Azul Zulu JDK
RUN apt-get update && \
    apt-get install -y zulu21-jdk

# Copy the JPro distribution to the image
ADD . jproserver/

# Start the JPro server
CMD (cd jproserver/; ./bin/restart.sh)