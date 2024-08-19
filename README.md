# Komet-java

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)

[Komet](https://www.ikm.dev/platform) was created to harmonize existing medical terminology and create interoperable
data. This file will explain best practices for getting started, building, running, and contributing code in Komet.

## Getting Started

To get started with using Komet, [download the latest version](https://www.ikm.dev/install) on your machine. See the
documentation about the new features recently included.

Follow the instructions below to set up the local environment for Komet:

1. Download and install Open JDK Java 19

2. Download and install Apache Maven 3.9 or greater

3. Prior to building Komet, there are additional repositories to clone and build. Please use
   the [`tinkar-core` README](https://github.com/ikmdev/tinkar-core/blob/main/README.md) file to build the `tinkar-core`
   project and its prerequisites before building `komet`.

## Building and Running Komet

Follow the steps below to build and run Komet on your local machine:

1. Clone the [komet repository](https://github.com/ikmdev/komet) from GitHub to your local machine

2. Change local directory to location to `komet`

3. Enter the following command to build the application:

```bash
mvn clean install
```

4. Run the Komet application with the following command:

```bash
mvn -f application javafx:run
```

Note: Komet requires sample data to operate with full functionality

## Issues and Contributions

Technical and non-technical issues can be reported to the Issue Tracker (LINK TO ISSUE TRACKER ONCE PUT IN KOMET REPO).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.
