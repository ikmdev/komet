# Komet TestFX Page Object Model Framework

## Overview

This framework implements the **Page Object Model (POM)** pattern for Komet application testing using TestFX. The POM pattern promotes code reusability, maintainability, and readability by separating test logic from UI interaction code.

## Architecture

### Directory Structure

```
src/test/java/dev/ikm/komet/app/test/integration/testfx/
├── pages/                          # Page Object classes
│   ├── BasePage.java              # Base class with common functionality
│   ├── DataSourceSelectionPage.java
│   ├── LoginPage.java
│   ├── LandingPage.java
│   ├── JournalPage.java
│   ├── NavigatorPanel.java        # Includes search functionality
│   └── ConceptDetailsPage.java
├── utils/                          # Utility classes
│   └── TestReporter.java         # Screenshot and reporting utilities
├── KometUserWorkflowITestFX.java  # Original test (legacy)
└── KometUserWorkflowRefactoredTest.java  # Refactored test using POM
```

## Components

### 1. BasePage

The `BasePage` class provides common functionality shared across all page objects:

- **Navigation**: `clickOn()`, `clickOnText()`, `doubleClickOnText()`
- **Input**: `type()`, `pressKey()`
- **Utilities**: `waitFor()`, `closeDialogs()`, `lookup()`, `captureScreenshot()`
- **Logging**: Integrated SLF4J logging
- **Screenshot Capture**: Automatic dialog screenshots before closing

**Example:**
```java
public class MyPage extends BasePage {
    public MyPage(FxRobot robot) {
        super(robot);
    }
    
    public MyPage clickButton() {
        clickOnText("Button Text");
        return this;
    }
}
```

### 2. Page Objects

Each page object represents a specific screen or component in the application:

#### DataSourceSelectionPage
Handles data source selection at application startup.
```java
new DataSourceSelectionPage(robot)
    .selectDataSource("komet")
    .clickOk();
```

#### LoginPage
Manages user authentication.
```java
new LoginPage(robot)
    .selectUser("KOMET user")
    .enterPassword("password")
    .signIn();
```

#### LandingPage
Controls main application screen operations with intelligent scrolling.
```java
LandingPage landingPage = new LandingPage(robot);
landingPage.maximizeWindow();
landingPage.createProjectJournal();  // Automatically scrolls in journal pane
landingPage.closeJournalWindow();
```

#### JournalPage
Handles journal-specific operations.
```java
new JournalPage(robot)
    .dragKometUserToEditingArea()
    .resizeConceptPane();
```

#### NavigatorPanel
Manages tree navigation, search operations, and concept selection.
```java
NavigatorPanel navigator = new NavigatorPanel(robot);

// Navigation operations
navigator
    .clickNextgenNavigator()
    .clickConcepts()
    .expandTreeNode("Author")
    .openConcept("Gretel");

// Search operations
navigator
    .clickNextgenSearch()
    .search("user")
    .openConceptFromResults("KOMET user");

// Drag and drop operations
navigator.dragToEditingArea("KOMET user");
```

#### ConceptDetailsPage
Manages concept editing and updates with retry logic for UI elements.
```java
new ConceptDetailsPage(robot)
    .editOtherName()
    .updateName("New Name")
    .submit();  // Includes scroll fallback if button not immediately visible
```

### 3. TestReporter

Utility class for screenshots and ExtentReports integration:

```java
TestReporter reporter = new TestReporter(screenshotDir, reportDir, robot);
reporter.createTest("Test Name", "Description");
reporter.logStepWithScreenshot("Step description");
reporter.saveScreenshot("screenshot-name");
reporter.flush();
```

## Benefits of Page Object Model

### 1. **Maintainability**
- UI changes only require updates to page objects, not every test
- Centralized element locators
- Single source of truth for UI interactions

### 2. **Reusability**
- Page objects can be shared across multiple tests
- Common operations abstracted in BasePage
- Method chaining enables fluent API

### 3. **Readability**
- Tests read like user stories
- Business logic separated from technical details
- Self-documenting code

### 4. **Scalability**
- Easy to add new page objects
- Modular design supports parallel development
- Reduces code duplication

### 5. **Robustness**
- Built-in retry logic and fallback strategies
- Automatic screenshot capture before closing dialogs
- Intelligent scrolling for hard-to-reach elements

## Writing Tests

### Example Test Using Page Objects

```java
@Test
public void testUserWorkflow(FxRobot robot) throws Exception {
    // Given: User is on data source selection page
    DataSourceSelectionPage dataSourcePage = new DataSourceSelectionPage(robot);
    
    // When: User selects data source and logs in
    LandingPage landingPage = dataSourcePage
        .selectDataSource("komet")
        .clickOk()
        .selectUser("KOMET user")
        .enterPassword("password")
        .signIn();
    
    // And: User creates a journal
    JournalPage journalPage = landingPage.createProjectJournal();
    
    // And: User navigates to a concept
    ConceptDetailsPage conceptPage = landingPage
        .openNavigator()
        .expandTreeNode("Author")
        .openConcept("Gretel");
    
    // Then: User can edit the concept
    conceptPage
        .updateName("New Name")
        .submit();
    
    reporter.logStepWithScreenshot("Test completed");
}
```

### Comparison: Before vs After

#### Before (Original Test)
```java
// 50+ lines of robot.clickOn(), robot.moveTo(), etc.
Button okButton = lookupNode(robot, SELECTOR_OK_BUTTON, Button.class);
robot.clickOn(okButton);
waitForFxEvents();
LOG.info("Step 3: Clicked 'OK' button");
saveScreenshot(robot, "after_ok_click");
// ... more boilerplate code
```

#### After (With Page Objects)
```java
// Clean, readable, maintainable
new DataSourceSelectionPage(robot)
    .selectDataSource("komet")
    .clickOk();
reporter.logStepWithScreenshot("Step 3: Selected data source");
```

## Best Practices

### 1. **Method Chaining**
Return `this` or the next page object to enable fluent API:
```java
public LoginPage selectUser(String username) {
    // ... implementation
    return this;  // Enable chaining
}
```

### 2. **Page Transitions**
Return the appropriate page object when navigating:
```java
public LandingPage signIn() {
    // ... click sign-in button
    return new LandingPage(robot);  // Return next page
}
```

### 3. **Logging**
Use descriptive log messages:
```java
LOG.info("Selected user: {}", username);
LOG.error("Failed to find element: {}", selector);
```

### 4. **Wait Strategies**
Use appropriate waits for UI synchronization:
```java
waitFor(1000);  // Short wait
waitForFxEvents();  // JavaFX event queue
```

### 5. **Error Handling**
Implement fallback strategies:
```java
try {
    clickOn(SELECTOR_SUBMIT_BUTTON);
} catch (Exception e) {
    clickOnText("Submit");  // Fallback
}
```

## Migration Guide

To migrate existing tests to use page objects:

1. **Identify UI Sections**: Group related operations
2. **Create Page Objects**: Extend BasePage for each section
3. **Extract Methods**: Move robot interactions into page methods
4. **Update Tests**: Replace direct robot calls with page object methods
5. **Add Reporting**: Integrate TestReporter for screenshots

## Running Tests

### Prerequisites

**Important**: The tests require Java 24 or higher. Make sure to set `JAVA_HOME` before running tests:

**Windows (PowerShell):**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
```

**Windows (Command Prompt):**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-25
```

**Linux/Mac:**
```bash
export JAVA_HOME=/path/to/jdk-25
```

### Run Workflow Test
```bash
mvn test -pl application -DrunITestFX -Dtest=KometUserWorkflowRefactoredTest -Dtestfx.headless=false
```

### Run with Specific Application Version
To test against a specific Komet version, set the `komet.app.version` property:
```bash
mvn test -pl application -DrunITestFX -Dtest=KometUserWorkflowRefactoredTest -Dtestfx.headless=false -Dkomet.app.version=1.59.0
```

This is useful for testing different releases or tracking which version was tested in reports.

### Run All Tests
```bash
mvn test -pl application -DrunITestFX
```

### Screenshot Location

Dialog and test step screenshots are automatically saved to:
```
C:\Users\<username>\Solor\test-screenshots\
```

Filename format: `yyyyMMdd_HHmmss_SSS_description.png`

### Troubleshooting

**Build fails with "Unsupported major.minor version 68.0"**
- Ensure `JAVA_HOME` points to JDK 24+ before running Maven
- Check Maven is using correct Java: `mvn --version`

**Tests fail to start**
- Run full build first: `mvn clean install -DskipTests`
- Verify all modules compiled successfully

**JavaFX not visible during test**
- Ensure `-Dtestfx.headless=false` flag is set
- Check display settings on headless systems

## Resources

- [TestFX Documentation](https://github.com/TestFX/TestFX)
- [Page Object Model Pattern](https://martinfowler.com/bliki/PageObject.html)
- [ExtentReports](https://www.extentreports.com/)