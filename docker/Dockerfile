FROM azul/zulu-openjdk:21-latest

# Update the package list
RUN apt update

# Install xorg and libgtk-3-0 needed to run JPro applications
RUN apt install -y xorg libgtk-3-0

# Copy the JPro distribution to the image
ADD . jproserver/

# Start the JPro server
CMD (cd jproserver/; ./bin/restart.sh)