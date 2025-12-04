# tinkar-core

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)


Tinkar core is an essential repository that creates the primitive functionality and data types for Komet. Tinkar core is also responsible for managing caches and I/O (spined-array) of the application. Building `tinkar-core` is a prerequisite to building `komet` on your local machine.

### Team Ownership - Product Owner
Data Team

## Getting Started

Follow the instructions below to set up the local environment for `tinkar-core`:

1. Download and install Open JDK Java 24

2. Download and install Apache Maven 3.9.11 or greater

3. Download and install Git

4. There are dependencies to building `tinkar-core`. Please ensure you have a reliable internet connection when cloning and building to get all dependencies from mvn central.

## Building and Running Tinkar Core

Follow the steps below to build and run `tinkar-core` on your local machine:

1. Clone the [tinkar-core repository](https://github.com/ikmdev/tinkar-core) from GitHub to your local machine

```bash
git clone [Repo URL]
```

2. Change local directory to location to `tinkar-core`

3. Enter the following command to build the application:

```bash
mvn clean install
```

4. To build and run Komet, please refer to the Komet README [LINK]

## Issues and Contributions
Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/tinkar-core/issues).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.
