# Komet-java

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)

[Komet](https://www.ikm.dev/platform) was created to harmonize existing medical terminology and create interoperable
data. This file will explain best practices for getting started, building, running, and contributing code in Komet.

### Team Ownership - Product Owner
Install App Team

## Getting Started
To get started with using Komet, [download the latest version](https://www.ikm.dev/install) on your machine. See the
documentation about the new features recently included.

Follow the instructions below to set up the local environment for Komet:
1. Download and install Open JDK Java 21
2. Download and install Apache Maven 3.9 or greater
3. Prior to building Komet, there are additional repositories to clone and build. Please use
   the [`tinkar-core` README](https://github.com/ikmdev/tinkar-core/blob/main/README.md) file to build the `tinkar-core`
   project and its prerequisites before building `komet`.

## Building and Running Komet
Follow these steps to build and run Komet on your local machine:
1. Clone the [komet-jpro repository](https://github.com/Sandec/komet-jpro) from GitHub to your local machine.
2. Navigate to the `komet-jpro` directory.
3. Build the application by executing the following command:
   ```bash
   mvn clean install
   ```
4. Run the Komet application using the following command:
   ```bash
   mvn -f application javafx:run
   ```

## Running Komet with JPro locally
After building Komet, you can run it with JPro on your local machine by following these steps:
1. Execute the following command to run the Komet application in your web browser:
   ```bash
   mvn -f application jpro:run
   ```
   The default web browser should open automatically, displaying the Komet application. If it doesn't, navigate
   to `http://localhost:8080` in your browser.
2. To stop the application, press `Ctrl + C` in the terminal where the application is running.
3. To rerun the application, repeat step 1.

## Running Komet with JPro in a Docker Container
To run Komet with JPro in a Docker container, follow these steps:
1. Create the application release zip for deployment using the following command:
   ```bash
   mvn clean -f application jpro:release
   ```
   The release zip will be created in the `application/target` directory, named `application-jpro.zip`.
2. Transfer the `application-jpro.zip` file to the directory where you want to run the Docker container.
3. Extract the contents of `application-jpro.zip` file and navigate to the extracted folder.
4. To run the application in a Docker container, choose one of the following options:
    * **Option 1**: Build the Docker image and run the Docker container manually
        * Inside the unzipped directory, locate the `Dockerfile`.
        * Build the Docker image with the following command:
          ```bash
          docker build -t komet-jpro .
          ```
        * Run the Docker container using the following command:
          ```bash
          docker run -d -v ~/Solor:/root/Solor -p 8080:8080 komet-jpro
          ```
          Note: `-v ~/Solor:/root/Solor`: This option mounts a volume, mapping a directory on your host machine to
          a directory inside the container.
            * **~/Solor**: Path to the dataset directory on your local system.
            * **/root/Solor**: Path inside the container where the dataset will be accessible.
    * **Option 2**: Use Docker Compose
        * Within the extracted directory, find the `docker-compose.yml` file.
        * Start the Docker container with Docker Compose by running:
          ```bash
          docker compose up -d
          ```
5. The application should now be running in the Docker container. Access it by navigating to `http://localhost:8080`
   in your web browser. If running on a remote server, replace localhost with the server’s IP address.

**Note:** Komet requires sample data to function fully.

## Running Komet GUI Tests Using the TestFX Framework
The Komet application includes GUI tests built with the TestFX framework. By default, these tests run in headless mode, 
which is ideal for continuous integration (CI) environments or situations where graphical interaction is unnecessary.
1. **Running TestFX Tests in Headless Mode (Default)**<br>
   To execute all unit tests, including the TestFX GUI tests, in headless mode (without launching a GUI window), run:
   ```bash
   mvn test
   ```
2. **Running TestFX Tests in Graphical Mode (Non-Headless)**<br>
   If you need to observe the GUI during testing—for instance, when debugging UI components—you can disable 
   headless mode by setting the headless property to false.<br>
   To run all tests in non-headless mode:
   ```bash
   mvn test -Dheadless=false
   ```
3. **Running Specific Tests**<br>
   To run a specific test class in a specific module, for example the `LoginTest` class in the `kview` module:
   ```bash
   mvn test -pl kview -Dtest=LoginTest -Dheadless=false
   ```
   To run a specific test method inside a specific class, for example the `testSuccessfulAuthentication` method in the
   `LoginTest` class in the `kview` module:
     ```bash
    mvn test -pl kview -Dtest=LoginTest#testSuccessfulAuthentication -Dheadless=false
     ```
**Important Note on Test Execution**<br>
The tests will only run once after they pass successfully. To trigger the tests again, changes must be made 
to any part of the project.

## Issues and Contributions
Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/komet/issues).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.

