This project uses the JPMS test framework for Maven as outlined in 
[Using Java Modularity (JPMS) in Tests](https://maven.apache.org/surefire/maven-failsafe-plugin/examples/jpms.html).
See the example for JUnit5 at the end of the page.

This approach creates a separate module for the test classes. As such, this is for "black box" testing, that is, the public methods of the project. That is adequate for this project.

In Eclipse, the module-info file in src/test/java is unsupported and generates an error. See [this thread](https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1465). To get around this exclude the module-info.java file from the build path.


### To run in Eclipse

Configure:
* Java build path
* Source
* src/test/java
* Exclude module-info.java

This gets a bit more complicated since this project is itself a set of test classes. 

So here src/main/java/module-info.java is:

```
open module dev.ikm.tinkar.reasoner.elksnomed.test {

```
and src/test/java/module-info.java is:

```
open module dev.ikm.tinkar.reasoner.elksnomed.test2 {
...
	requires transitive org.junit.jupiter.api;
	requires transitive org.junit.jupiter.engine;
```

Additionally, this project uses the JUnit5 API in src/main/java. The JUnit5 test runner in Eclipse runs in the unnamed module, and causes access violation errors with the platform module. Here is the workaround.

Add the following to the failsafe argument line:
* --add-exports org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED
* --add-exports org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED

Alternatively, could modify each run configuration VM arguments with the above.
