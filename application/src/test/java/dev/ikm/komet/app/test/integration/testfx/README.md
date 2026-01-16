# Komet TestFX Page Object Model Framework

## Overview

This framework implements the **Page Object Model (POM)** pattern for Komet application testing using TestFX. The POM pattern promotes code reusability, maintainability, and readability by separating test logic from UI interaction code.

## Architecture

### Directory Structure

```
src/test/java/dev/ikm/komet/app/test/integration/testfx/
├── config/                         # Configuration classes
│   └── TestConfiguration.java     # Central configuration for all tests
├── pages/                          # Page Object classes
│   ├── BasePage.java              # Base class with common functionality
│   ├── DataSourceSelectionPage.java
│   ├── LoginPage.java
│   ├── LandingPage.java
│   ├── NavigatorPanel.java        # Includes search functionality
│   ├── ConceptPane.java           # Concept editing and details
│   └── GitHubConnectionPage.java  # GitHub connection dialog
├── helpers/
│   └── workflows/                 # Workflow helper classes
│       ├── BaseWorkflow.java     # Base class for workflows
│       ├── Login.java            # Login workflow
│       ├── AuthorConcepts.java   # Concept authoring workflow
│       ├── Reasoner.java         # Reasoner workflow
│       ├── GithubConnection.java # GitHub connection workflow
│       └── GenerateDeXData.java  # DeX data generation workflow
├── utils/                          # Utility classes
│   ├── TestReporter.java         # Screenshot and reporting utilities
│   └── CredentialsReader.java    # CSV credentials management
├── DeXAuthoringProcessTest.java   # Main DeX authoring test
├── test-credentials-sample.csv    # Sample credentials file
└── README.md                      # This file
```

## Components

### 1. TestConfiguration

The `TestConfiguration` class is the central configuration hub that all test classes, page objects, and workflows inherit from. It provides access to:

- **Directory Configuration**: `BASE_DATA_DIR`, `SOLOR_DIR`, `TEST_SCREENSHOTS_DIR`, `EXTENT_REPORTS_DIR`
- **Version Configuration**: `APP_VERSION`
- **Data Source Configuration**: `DATA_SOURCE_NAME`, `DATA_SOURCE_STORE`
- **User Credentials**: `GITHUB_USERNAME`, `GITHUB_PASSWORD`, `USERNAME`, `PASSWORD`
- **GitHub Configuration**: `GITHUB_REPO_URL`, `GITHUB_EMAIL`
- **UI Selectors**: All CSS selectors used across the application
- **Test Data**: `SUPPLIED_BRAND_NAME`, `SUPPLIED_VERSION_NUMBER`, etc.
- **Test Toggles**: `enableInfo`, `enableSync` - Control GitHub operations via command line

**Key Features:**
- All configuration constants accessible via inheritance
- Protected constructor allows subclassing
- Centralized credential management via CSV file
- System property overrides supported
- Command-line toggles for optional test steps

**Example:**
```java
public class MyTest extends TestConfiguration {
    @Test
    public void testSomething() {
        // Direct access to all configuration constants
        String version = APP_VERSION;
        String dataDir = BASE_DATA_DIR;
        String username = GITHUB_USERNAME;
        
        // Check if optional steps are enabled
        if (enableInfo) {
            // Execute info step
        }
    }
}
```

### 2. BasePage

The `BasePage` class extends `TestConfiguration` and provides common functionality shared across all page objects:

- **Navigation**: `clickOn()`, `clickOnText()`, `doubleClickOnText()`
- **Input**: `type()`, `pressKey()`
- **Utilities**: `waitFor()`, `waitForText()`, `closeDialogs()`, `lookup()`, `captureScreenshot()`
- **Logging**: Integrated SLF4J logging
- **Screenshot Capture**: Automatic dialog screenshots before closing
- **Configuration Access**: Inherits all constants from TestConfiguration

**Example:**
```java
public class MyPage extends BasePage {
    public MyPage(FxRobot robot) {
        super(robot);
    }
    
    public MyPage clickButton() {
        // Access to configuration constants
        clickOn(SELECTOR_SUBMIT_BUTTON);
        return this;
    }
}
```

### 3. BaseWorkflow

The `BaseWorkflow` class extends `TestConfiguration` and provides common functionality for workflow helper classes:

- **Page Object Management**: Pre-initialized page objects (`conceptPane`, `landingPage`, `navigator`, etc.)
- **Scrolling Utilities**: `verticalScroll()`, `horizontalScroll()`
- **Wait Utilities**: `waitForMillis()`
- **App State Assertions**: `assertInitialAppState()`, `assertSelectUserState()`, `assertRunningAppState()`
- **Configuration Access**: Inherits all constants from TestConfiguration
- **Reporter Access**: TestReporter for logging test steps

**Example:**
```java
public class MyWorkflow extends BaseWorkflow {
    public MyWorkflow(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }
    
    public void performWorkflow() {
        reporter.logStep("Starting workflow");
        // Use pre-initialized page objects
        conceptPane.editDescription();
        // Access configuration
        String version = APP_VERSION;
    }
}
```

### 4. Page Objects

Each page object extends `BasePage` and represents a specific screen or component in the application:

#### DataSourceSelectionPage
Handles data source selection at application startup with app state checking.
```java
new DataSourceSelectionPage(robot)
    .selectDataSource("Open SpinedArrayStore")
    .clickOkButton();
```

**Features:**
- Checks app state before attempting operations
- Supports early return if already past data source selection
- Multiple fallback strategies for button clicking

#### LoginPage
Manages user authentication.
```java
new LoginPage(robot)
    .selectUser("KOMET user")
    .enterPassword("test")
    .clickSignIn();
```

#### LandingPage
Controls main application screen operations.
```java
LandingPage landingPage = new LandingPage(robot);
landingPage.maximizeWindow();
landingPage.openJournalWindow();
landingPage.closeJournalWindow();
```

#### NavigatorPanel
Manages navigation panel, search operations, and concept selection.
```java
NavigatorPanel navigator = new NavigatorPanel(robot);

// Navigation operations
navigator
    .openNextgenNavigator()
    .openNextgenSearch()
    .clickCreate()
    .clickConcepts();
```

#### ConceptPane
Manages concept editing and details with intelligent field selection.
```java
new ConceptPane(robot)
    .openPropertiesToggle()
    .clickEditDescriptions()
    .searchForParentConcept("Clinical finding")
    .publishConcept();
```

**Key Features:**
- Always selects the correct text field by top bounds (lowest on screen)
- Supports multiple text fields with same prompt text
- Detailed logging of field positions for debugging
- Automatic retries and fallback strategies

#### GitHubConnectionPage
Manages GitHub connection dialog interactions.
```java
new GitHubConnectionPage(robot, reporter)
    .enterGitHubCredentials(repoUrl, email, username, password)
    .clickInfo()
    .clickSync();
```

**Key Features:**
- Enters GitHub repository credentials
- Handles Info button clicks (displays connection information)
- Handles Sync button clicks (synchronizes with remote repository)
- Optional reporter for enhanced logging

### 5. Workflow Helpers

Workflow classes extend `BaseWorkflow` and encapsulate complex multi-step operations:

#### Login
Handles complete login flow with data source and user selection.
```java
Login loginWorkflow = new Login(robot, reporter);
loginWorkflow.loginWithDataSourceAndUser(
    DATA_SOURCE_NAME, 
    USERNAME, 
    PASSWORD
);
```

#### AuthorConcepts
Manages the complete concept authoring workflow.
```java
AuthorConcepts authorWorkflow = new AuthorConcepts(robot, reporter);
authorWorkflow.createAndPublishConcept(
    "New Concept",
    "Clinical finding",
    "This is a description"
);
```

#### Reasoner
Handles reasoner operations and monitors completion.
```java
Reasoner reasonerWorkflow = new Reasoner(robot, reporter);
reasonerWorkflow.runReasoner();
// Waits for "No tasks running" before proceeding
```

**Key Features:**
- Monitors Activity view for task completion
- Waits up to 2 minutes for reasoner to complete
- Polls every 2 seconds for "No tasks running" text
- Detailed progress logging

#### GithubConnection
Manages GitHub connection setup and operations.
```java
GithubConnection githubWorkflow = new GithubConnection(robot, reporter);
githubWorkflow.connectToGitHub(repoUrl, email, username, password);
githubWorkflow.clickInfoButton();  // Optional
githubWorkflow.clickSyncButton();  // Optional
```

**Key Features:**
- Opens Exchange menu and Info panel
- Enters GitHub credentials
- Connects to repository
- Provides separate methods for Info and Sync operations
- Both operations can be controlled via command-line flags

#### GenerateDeXData
Orchestrates complete DeX data generation workflow.
```java
GenerateDeXData dexWorkflow = new GenerateDeXData(robot, reporter);
dexWorkflow.generateDeXData(
    "Albumin Gen2 5166861190",
    "Roche Diagnostics COBAS Integra Albumin Gen.2"
);
```

**Key Features:**
- Opens new journal for data entry
- Searches for devices by supplied brand name
- Adds device names as alternate descriptions
- Runs reasoner automatically
- Optionally clicks Info and Sync based on command-line flags
- Closes journal upon completion

### 6. TestReporter

Utility class for screenshots and ExtentReports integration:

```java
TestReporter reporter = new TestReporter(reportPath, testName);
reporter.logBeforeStep("Step description");
reporter.logAfterStep("Step completed");
reporter.logStepPass("Step passed");
reporter.logStepFail("Step failed");
reporter.logFailure("Operation", exception);
reporter.flush();
```

### 7. CredentialsReader

Utility class for reading credentials from CSV file located at:
```
C:\Users\<YourUsername>\Solor\test-credentials.csv
```

**CSV Format:**
```csv
# Test Credentials Configuration
# Format: key,value

# GitHub Credentials
github_repo_url,https://github.com/ikmdev/komet.git
github_email,your-email@example.com
github_username,YourGitHubUsername
github_password,YourGitHubPassword

# Komet User Credentials
komet_username,KOMET user
komet_password,your_komet_password
```

**Usage:**
```java
CredentialsReader credentialsReader = new CredentialsReader();
String username = credentialsReader.get("github_username", "default");
String password = credentialsReader.get("github_password", "default");
```

**Setup Instructions:**
1. **Create the credentials directory:**
```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\Solor"
```

2. **Copy the sample file:**
```powershell
Copy-Item "c:\Desktop Automation\Komet\komet-main\application\test-credentials-sample.csv" "$env:USERPROFILE\Solor\test-credentials.csv"
```

3. **Edit with your actual credentials:**
```powershell
notepad "$env:USERPROFILE\Solor\test-credentials.csv"
```

**Key Features:**
- CSV-based configuration (easier to edit than properties files)
- Automatic fallback to default values
- Comments supported (lines starting with #)
- Secure storage outside project directory
- Single location for all test credentials

## Configuration Management

### TestConfiguration Inheritance

All test classes, page objects, and workflows extend `TestConfiguration` to access shared constants:

```java
// Test class
public class DeXAuthoringProcessTest extends TestConfiguration {
    // Has access to APP_VERSION, BASE_DATA_DIR, USERNAME, etc.
}

// Page object
public class ConceptPane extends BasePage {
    // BasePage extends TestConfiguration
    // Has access to all selectors and configuration
}

// Workflow
public class Login extends BaseWorkflow {
    // BaseWorkflow extends TestConfiguration
    // Has access to all configuration constants
}
```

### System Property Overrides

Override configuration at runtime using system properties:

```bash
-Dkomet.app.version=1.59.0
-Dkomet.data.directory=/custom/path
-Dkomet.data.source.store="Custom Store"
-Dtarget.data.directory=/custom/test/dir
```

### Command-Line Test Control

The framework supports command-line toggles to enable/disable specific test steps:

#### Control GitHub Operations

**Enable/Disable Info Button:**
```bash
-DenableInfo=true   # Enable Info button clicks (default)
-DenableInfo=false  # Disable Info button clicks
```

**Enable/Disable Sync Button:**
```bash
-DenableSync=true   # Enable Sync button clicks (default)
-DenableSync=false  # Disable Sync button clicks
```

**Example Commands:**
**Run with both Info and Sync enabled (default):**
```powershell
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false
```

**Run with Info enabled, Sync disabled:**
```powershell
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false -DenableSync=false
```

**Run with both Info and Sync disabled:**
```powershell
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false -DenableInfo=false -DenableSync=false
```

**Complete PowerShell command with Java 25:**
```powershell
cd 'c:\Desktop Automation\Komet\komet-main'; $env:JAVA_HOME = "C:\Program Files\Java\jdk-25"; mvn test -pl application -DrunITestFX "-Dtest=DeXAuthoringProcessTest" "-Dtestfx.headless=false" "-DenableInfo=true" "-DenableSync=false"
```

**Expected Log Output:**
When enabled:
```
✓ Click Info Button: COMPLETE
✓ Click Sync Button: COMPLETE
```
When disabled:
```
⊘ Click Info Button: SKIPPED
⊘ Click Sync Button: SKIPPED
```

**Use Cases:**
- Skip Sync during rapid development/testing to save time
- Skip Info to streamline test execution
- Enable both for full integration testing
- Control which GitHub operations run based on test environment

## Benefits of Page Object Model

### 1. **Maintainability**
- UI changes only require updates to page objects, not every test
- Centralized element locators in TestConfiguration
- Single source of truth for UI interactions

### 2. **Reusability**
- Page objects can be shared across multiple tests
- Common operations abstracted in BasePage and BaseWorkflow
- Method chaining enables fluent API
- Configuration inherited across entire hierarchy

### 3. **Readability**
- Tests read like user stories
- Business logic separated from technical details
- Self-documenting code
- Clear workflow steps with reporter logging

### 4. **Scalability**
- Easy to add new page objects and workflows
- Modular design supports parallel development
- Reduces code duplication through inheritance

### 5. **Robustness**
- Built-in retry logic and fallback strategies
- Automatic screenshot capture before closing dialogs
- Intelligent element selection (e.g., lowest text field)
- App state checking before operations
- Task completion monitoring for long-running operations

### 6. **Flexibility**
- Command-line control of optional test steps
- CSV-based credential management
- System property overrides
- Environment-specific configuration

## Writing Tests

### Example Test Using Page Objects and Workflows

```java
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeXAuthoringProcessTest extends TestConfiguration {

    private FxRobot robot;
    private TestReporter reporter;
    private Login loginWorkflow;
    private AuthorConcepts authorConceptsWorkflow;
    private Reasoner reasonerWorkflow;
    private GithubConnection githubConnection;

    @BeforeAll
    public static void setUpClass() {
        // Read command-line parameters
        enableInfo = Boolean.parseBoolean(System.getProperty("enableInfo", "true"));
        enableSync = Boolean.parseBoolean(System.getProperty("enableSync", "true"));
        
        LOG.info("Click Info enabled: {}", enableInfo);
        LOG.info("Click Sync enabled: {}", enableSync);
    }

    @BeforeEach
    public void setUp(FxRobot robot) throws Exception {
        this.robot = robot;
        
        // Initialize reporter
        String reportPath = BASE_DATA_DIR + File.separator + SOLOR_DIR + 
                          File.separator + EXTENT_REPORTS_DIR;
        reporter = new TestReporter(reportPath, "DeX Authoring Test");
        
        // Initialize workflows
        loginWorkflow = new Login(robot, reporter);
        authorConceptsWorkflow = new AuthorConcepts(robot, reporter);
        reasonerWorkflow = new Reasoner(robot, reporter);
        githubConnection = new GithubConnection(robot, reporter);
        
        // Launch application
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(App.class);
    }

    @Test
    @Order(1)
    public void testDeXAuthoringProcess() throws Exception {
        // Step 1: Login
        loginWorkflow.loginWithDataSourceAndUser(
            DATA_SOURCE_NAME, 
            USERNAME, 
            PASSWORD
        );
        
        // Step 2: Author a concept
        authorConceptsWorkflow.createAndPublishConcept(
            "New Clinical Finding",
            "Clinical finding",
            "A description of the new concept"
        );
        
        // Step 3: Run reasoner
        reasonerWorkflow.runReasoner();
        
        // Step 4: Optional GitHub Info
        if (enableInfo) {
            githubConnection.clickInfoButton();
            LOG.info("✓ Click Info Button: COMPLETE");
        } else {
            LOG.info("⊘ Click Info Button: SKIPPED");
        }
        
        // Step 5: Optional GitHub Sync
        if (enableSync) {
            githubConnection.clickSyncButton();
            LOG.info("✓ Click Sync Button: COMPLETE");
        } else {
            LOG.info("⊘ Click Sync Button: SKIPPED");
        }
        
        reporter.logStepPass("DeX authoring process completed successfully");
    }

    @AfterEach
    public void tearDown() {
        if (reporter != null) {
            reporter.flush();
        }
    }
}
```

### Comparison: Before vs After

#### Before (Direct Robot Calls)
```java
// 50+ lines of robot.clickOn(), robot.moveTo(), etc.
Button okButton = robot.lookup(SELECTOR_OK_BUTTON).queryButton();
robot.clickOn(okButton);
waitForFxEvents();
LOG.info("Step 3: Clicked 'OK' button");
Thread.sleep(1000);
TextField searchField = robot.lookup(".text-field").query();
robot.clickOn(searchField);
robot.write("Clinical finding");
// ... more boilerplate code
```

#### After (With Page Objects and Workflows)
```java
// Clean, readable, maintainable
loginWorkflow.loginWithDataSourceAndUser(
    DATA_SOURCE_NAME, 
    USERNAME, 
    PASSWORD
);

conceptPane
    .searchForParentConcept("Clinical finding")
    .publishConcept();

// Optional steps controlled by command line
if (enableInfo) {
    githubConnection.clickInfoButton();
}

reporter.logStepPass("Concept published successfully");
```

## Best Practices

### 1. **Extend TestConfiguration**
All test classes should extend TestConfiguration for access to shared constants:
```java
public class MyTest extends TestConfiguration {
    // Automatically has access to all configuration
}
```

### 2. **Use Workflows for Complex Operations**
Encapsulate multi-step operations in workflow classes:
```java
public class MyWorkflow extends BaseWorkflow {
    public void performComplexOperation() {
        reporter.logBeforeStep("Starting complex operation");
        // Use pre-initialized page objects
        conceptPane.editDescription();
        navigator.openNextgenSearch();
        reporter.logAfterStep("Operation completed");
    }
}
```

### 3. **Method Chaining**
Return `this` or the next page object to enable fluent API:
```java
public ConceptPane searchForParentConcept(String concept) {
    // ... implementation
    return this;  // Enable chaining
}
```

### 4. **Intelligent Element Selection**
Use advanced strategies for element selection:
```java
// Select the text field with the greatest top bound (lowest on screen)
double maxTopBound = Double.NEGATIVE_INFINITY;
for (TextField tf : textFields) {
    double top = tf.localToScreen(tf.getBoundsInLocal()).getMinY();
    if (top > maxTopBound) {
        maxTopBound = top;
        searchField = tf;
    }
}
```

### 5. **Wait for Task Completion**
For long-running operations, poll for completion status:
```java
boolean completed = false;
long timeout = 120000; // 2 minutes
long startTime = System.currentTimeMillis();

while (!completed && (System.currentTimeMillis() - startTime) < timeout) {
    if (robot.lookup("No tasks running").tryQuery().isPresent()) {
        completed = true;
    } else {
        waitForMillis(2000); // Check every 2 seconds
    }
}
```

### 6. **App State Checking**
Check app state before performing operations:
```java
AppState currentState = App.state.get();
if (currentState != AppState.SELECT_DATA_SOURCE) {
    LOG.info("Already past data source selection");
    return;
}
```

### 7. **Comprehensive Logging**
Use detailed logging with position information:
```java
LOG.info("TextField #{} - Top: {}, Left: {}, Visible: {}", 
         index, (int)top, (int)left, tf.isVisible());
LOG.info("Selected LOWEST TextField (top={})", (int)maxTopBound);
```

### 8. **Fallback Strategies**
Implement multiple approaches for robustness:
```java
try {
    clickOn(SELECTOR_OK_BUTTON);
} catch (Exception e) {
    LOG.warn("Selector failed, trying text-based lookup");
    clickOnText("OK");
}
```

### 9. **Use Command-Line Toggles**
For optional or environment-specific steps:
```java
if (enableInfo) {
    githubConnection.clickInfoButton();
    LOG.info("✓ Click Info Button: COMPLETE");
} else {
    LOG.info("⊘ Click Info Button: SKIPPED");
}
```

### 10. **Secure Credential Management**
Store credentials in CSV file outside project:
```csv
github_username,YourGitHubUsername
github_password,YourSecurePassword
```

## Migration Guide

To migrate existing tests to use the new architecture:

1. **Extend TestConfiguration**: Make your test class extend TestConfiguration
2. **Use Workflows**: Replace direct page object calls with workflow methods
3. **Access Configuration**: Use inherited constants instead of hardcoded values
4. **Implement Retry Logic**: Add fallback strategies and intelligent element selection
5. **Add Task Monitoring**: For long operations, poll for completion
6. **Enhance Logging**: Add detailed logging with TestReporter
7. **Setup Credentials CSV**: Move credentials to CSV file in Solor directory
8. **Add Command-Line Toggles**: Use system properties for optional steps

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

### Setup Credentials

**Before running tests, setup your credentials CSV file:**

1. **Create directory:**
```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\Solor"
```

2. **Copy sample file:**
```powershell
Copy-Item "c:\Desktop Automation\Komet\komet-main\application\test-credentials-sample.csv" "$env:USERPROFILE\Solor\test-credentials.csv"
```

3. **Edit with your credentials:**
```powershell
notepad "$env:USERPROFILE\Solor\test-credentials.csv"
```

### Run DeX Authoring Test

**Basic test run:**
```bash
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false
```

**With Info enabled, Sync disabled:**
```bash
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false -DenableSync=false
```

**With both Info and Sync disabled:**
```bash
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false -DenableInfo=false -DenableSync=false
```

**Complete PowerShell command with Java 25:**
```powershell
cd 'c:\Desktop Automation\Komet\komet-main'; $env:JAVA_HOME = "C:\Program Files\Java\jdk-25"; mvn test -pl application -DrunITestFX "-Dtest=DeXAuthoringProcessTest" "-Dtestfx.headless=false" "-DenableInfo=true" "-DenableSync=false"
```

### Run with Specific Application Version
```bash
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false -Dkomet.app.version=1.59.0
```

### Run with Custom Data Directory
```bash
mvn test -pl application -DrunITestFX -Dtest=DeXAuthoringProcessTest -Dtestfx.headless=false -Dtarget.data.directory=/custom/test/dir
```

### Run All Tests
```bash
mvn test -pl application -DrunITestFX
```

### Output Locations

**Screenshots:**
```
<BASE_DATA_DIR>/Solor/test-screenshots-dex/
```

**Reports:**
```
<BASE_DATA_DIR>/Solor/extent-reports-dex/
```

**Logs:**
```
application/target/surefire-reports/
```

**Credentials:**
```
C:\Users\<YourUsername>\Solor\test-credentials.csv
```

### Troubleshooting

**Build fails with "Unsupported major.minor version 68.0"**
- Ensure `JAVA_HOME` points to JDK 24+ before running Maven
- Check Maven is using correct Java: `mvn --version`

**Tests fail with "EmptyNodeQuery"**
- Element not found - check selector in TestConfiguration
- Add longer waits with `waitForText()` method
- Implement fallback strategy with text-based lookup

**Tests fail with "cannot find symbol"**
- Ensure class extends TestConfiguration or BasePage/BaseWorkflow
- Check that constants are defined in TestConfiguration
- Verify imports are correct

**"No tasks running" timeout**
- Reasoner taking longer than 2 minutes
- Check Activity view is accessible
- Increase timeout in Reasoner workflow

**JavaFX not visible during test**
- Ensure `-Dtestfx.headless=false` flag is set
- Check display settings on headless systems

**Tests fail to start**
- Run full build first: `mvn clean install -DskipTests`
- Verify all modules compiled successfully

**Credentials not found**
- Verify CSV file exists: `C:\Users\<YourUsername>\Solor\test-credentials.csv`
- Check CSV format matches sample file
- Ensure no BOM or encoding issues

**GitHub operations fail**
- Check credentials in CSV file are correct
- Verify repository URL is accessible
- Check network connectivity
- Use `-DenableSync=false` to skip problematic operations

## Configuration Reference

### TestConfiguration Constants

**Directory Configuration:**
- `BASE_DATA_DIR` - Base directory for test data
- `SOLOR_DIR` - Solor subdirectory name
- `TEST_SCREENSHOTS_DIR` - Screenshot directory name
- `EXTENT_REPORTS_DIR` - Reports directory name

**Version Configuration:**
- `APP_VERSION` - Application version being tested

**Data Source Configuration:**
- `DATA_SOURCE_NAME` - Data source name
- `DATA_SOURCE_STORE` - Data source store type

**Credentials:**
- `GITHUB_USERNAME`, `GITHUB_PASSWORD` - GitHub credentials (from CSV)
- `USERNAME`, `PASSWORD` - Komet user credentials (from CSV)
- `GITHUB_REPO_URL`, `GITHUB_EMAIL` - GitHub configuration (from CSV)

**Test Toggles:**
- `enableInfo` - Enable/disable Info button clicks (default: true)
- `enableSync` - Enable/disable Sync button clicks (default: true)

**UI Selectors:** (See TestConfiguration.java for complete list)
- `SELECTOR_OK_BUTTON`, `SELECTOR_SIGN_IN_BUTTON`, etc.
- All CSS selectors for UI elements

### Command-Line Parameters

**Test Control:**
```bash
-DenableInfo=true|false   # Control Info button clicks
-DenableSync=true|false   # Control Sync button clicks
```

**Application Configuration:**
```bash
-Dkomet.app.version=1.59.0
-Dtarget.data.directory=/custom/test/dir
```

**TestFX Configuration:**
```bash
-Dtestfx.headless=false   # Show UI during test
-DrunITestFX              # Enable TestFX tests
```

## Resources

- [TestFX Documentation](https://github.com/TestFX/TestFX)
- [Page Object Model Pattern](https://martinfowler.com/bliki/PageObject.html)
- [ExtentReports](https://www.extentreports.com/)
- [JavaFX Documentation](https://openjfx.io/)
- [Maven System Properties](https://maven.apache.org/guides/mini/guide-configuring-maven.html#configuring-your-local-repository)