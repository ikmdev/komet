# Komet-java

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ikmdev_tinkar-core&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ikmdev_tinkar-core)

[Komet](https://www.ikm.dev/platform) was created to harmonize existing medical terminology and create interoperable
data. This file will explain best practices for getting started, building, running, and contributing code in Komet.

### Team Ownership - Product Owner
Install App Team

## Project Overview
This intention of this (Komet) project is to provide a user-friendly interface that lets the user view, analyze, change, create, 
import and export the various medical terminologies. All the terminology changes and their relationships are saved and can be 
viewed historically. 

# Getting Started
## Installation Instructions
### Pre-requisites:
1. You should have copy of a local DB available and configured. If not then please request for one. 
2. Uninstall any previously installed versions of Komet.

### Installing Komet on your local machine:
1. To get started with installing and using Komet, [browse to the available releases in GitHub](https://github.com/ikmdev/komet/releases). See the
documentation about the new features included in each release.![img_1.png](img_1.png)
2. Download the appropriate installation file for your machine (_example, for Windows OS download **Komet-X.X.X-Installer-Windows-Unsigned.msi**_).
3. Once the download is complete, run the downloaded file and install Komet. 
4. Follow the (if-any) installation instructions. For Windows if you get a security alert click on "**_Run anyway_**" option.![img_2.png](img_2.png)
5. Komet is now installed on your local machine. You can now run Komet from programs or installed directory.

## Follow the instructions below to set up the local environment for Komet
### Pre-requisites:
1. Local Git repo and GitBash or similar shell-prompt installed/configured on your local machine.
2. Set up GitHub by following the [instructions provided here](https://ikmdev.atlassian.net/wiki/spaces/IKM/pages/390201368/GitHub+Account+Creation+IKM+FDA+Shield)
3. Download and install Open JDK Java 21 or greater [latest version](https://openjdk.org/projects/jdk/)
4. Download and install Apache Maven 3.9 or greater [link here](https://maven.apache.org/download.cgi)
5. Prior to building Komet, there are additional repositories to clone and build. Please use
   the [`tinkar-core` README](https://github.com/ikmdev/tinkar-core/blob/main/README.md) file to build the `tinkar-core`
   project and its prerequisites before building `komet`.

### Building and Running Komet locally:
1. Once you have access to [komet repository](https://github.com/ikmdev/komet) on GitHub, fork the repository using instructions 
provided in "_**Fork the Repository**_" section in [GitHub document](https://ikmdev.atlassian.net/wiki/spaces/IKM/pages/390201368/GitHub+Account+Creation+IKM+FDA+Shield). 
2. Clone the forked Komet repository on your local machine by running the git bash command.
   ```bash
   git clone git@github.com:your-github-username/komet.git
   ```
3. Change the local directory location to `komet`
4. Enter the following command to build the application:
   ```bash
   mvn clean install
   ```

5. Run the Komet application with the following command:
   ```bash
   mvn -f application javafx:run
   ```
6. You can open Komet code using your favorite IDE like _Eclipse_ or _IntelliJ Idea_ and try running it from there. 
While running Komet UI from your IDE, you many have to add the following VM arguments: 
   ```
   -Xmx10g --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=dev.ikm.komet.navigator
   ```
 
## Usage Examples:
This section details on the basic design methodology used for developing nex-gen Komet UI.
1. Komet UI application is moving towards the nex-gen implementation which follows Model-View-View-Model (MVVM) design pattern.
2. Komet application design is event-based where the subscriber to an event listens for a particular event and when it is triggered,
desired logic can be executed in the listener code.
Example:
   ```java
   import java.util.UUID;
   
   public class MyController {
      private EvtBus eventBus;
      private Subscriber<MyDefienedEvent> someMyDefinedEventSubscriber;
   
      public void initialize() {
         someMyDefinedEventSubscriber = evt -> {
            // Some logic to process the event.
            if (evt.getEventType() == MyDefienedEvent.SOME_EVENT_1) {
               // do something.
            } else if (evt.getEventType() == MyDefienedEvent.SOME_EVENT_2) {
               //do something else.
            }
         };
         eventBus.subscribe(myTopic, MyDefienedEvent.class, someMyDefinedEventSubscriber);
      }
   }
   
   public class MyDefienedEvent extends Evt {
      public static final EvtType<MyDefienedEvent> SOME_EVENT_1 = new EvtType<>(Evt.ANY, "SOME_EVENT_1");
      public static final EvtType<MyDefienedEvent> SOME_EVENT_2 = new EvtType<>(Evt.ANY, "SOME_EVENT_2");
   
      /**
       * Constructs a prototypical Event.
       * You can optionally pass arguments in this constructor and set the value as final in the constructor.
       * The value can be retrived using the getter method for that variable.
       * @param source         the object on which the Event initially occurred
       * @param eventType
       */
      public MyDefienedEvent(Object source, EvtType eventType) {
         super(source, eventType);
      }
   }
   
   public class MySomeClass {
      private EvtBus eventBus;
      private UUID someTopic;
      
      public MySomeClass(UUID someTopic){
          this.someTopic = someTopic;
      }
      
      public void someMethod() {
         eventBus.publish(someTopic, new MyDefienedEvent(this, MyDefienedEvent.SOME_EVENT_1));
      }
   }
   
   public class MainClass {
      public static void main(String[] args) {
         MySomeClass mySomeClass = new MySomeClass(UUID.randomUUID());
      }
   }
   ```
4. Komet's design also includes the cognitive framework to implement MVVM architecture framework.
You can find more information along with the examples [here](https://github.com/carldea/cognitive/wiki) 
   1. Gradle:
      ```
      implementation 'org.carlfx:cognitive:1.3.0'
      ```
   2. Maven
      ```
      <dependency>
      <groupId>org.carlfx</groupId>
      <artifactId>cognitive</artifactId>
      <version>1.3.0</version>
      </dependency>
      ```
   3. Project using Java Modules (JPMS) will want to do the following in the consuming module:
       ```
         requires org.carlfx.cognitive;
       ```

## Configuration Options
1. No specific configuration is required to run the installed version of Komet.
2. To run Komet from an IDE (developerment environment), you will have to do some VM configuration as below:
   ```
   -Xmx10g --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=dev.ikm.komet.navigator
   ```
3. The DB needs to be configured under the '_**users -> SOLAR**_' directory.
4. Komet requires sample data to operate with full functionality

## Issues and Contributions
Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/komet/issues).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.

