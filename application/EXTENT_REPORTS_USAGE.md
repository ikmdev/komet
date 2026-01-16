# ExtentReports Usage Guide

## Overview
ExtentReports has been integrated into the Komet User Workflow TestFX tests. Reports are generated with base64-encoded screenshots for each test step.

## Report Location
Reports are stored in: `{BASE_DATA_DIR}/Solor/extent-reports/`

By default: `C:\Users\{username}\Solor\extent-reports\`

## Report File Naming
Reports are named with timestamp: `KometWorkflowTest_YYYYMMDD_HHMMSS.html`

## How to Add Screenshots to Test Steps

To log a test step with a screenshot in your test method, call:

```java
logStepWithScreenshot(robot, "Step description");
```

### Example Usage in Test Method

```java
@Test
@DisplayName("Complete Komet User Workflow Test")
public void testCompleteKometUserWorkflow(FxRobot robot) throws TimeoutException, InterruptedException {
    LOG.info("Starting Complete Komet User Workflow Test");

    // Step 1: Wait for application to launch
    assertInitialAppState();
    logStepWithScreenshot(robot, "Step 1: Application launched successfully");

    // Step 2: Click "komet" from data source list
    robot.clickOn("komet");
    waitForFxEvents();
    logStepWithScreenshot(robot, "Step 2: Clicked 'komet' from list");

    // Step 3: Click "OK" button
    Button okButton = lookupNode(robot, SELECTOR_OK_BUTTON, Button.class);
    robot.clickOn(okButton);
    waitForFxEvents();
    logStepWithScreenshot(robot, "Step 3: Clicked 'OK' button");

    // Continue for each step...
}
```

## Features

### Report Configuration
- **Theme**: Dark theme
- **Document Title**: "Komet User Workflow Test Report"
- **Report Name**: "Komet Automated Test Execution"
- **System Information**: 
  - Application: Komet
  - Environment: Test
  - User: Current system user
  - Java Version

### Screenshot Capture
- Screenshots are captured using TestFX CaptureSupport
- Converted to BufferedImage
- Encoded as Base64 strings
- Embedded directly in the HTML report (no external image files needed)

### Benefits
- **Self-contained reports**: All screenshots are embedded as base64, no external files
- **Easy sharing**: Single HTML file contains everything
- **Visual debugging**: See exactly what the application looked like at each step
- **Professional formatting**: ExtentReports provides a clean, modern interface

## Report Lifecycle

1. **@BeforeAll**: 
   - Creates extent-reports directory
   - Initializes ExtentReports instance
   - Creates HTML report file with timestamp

2. **@BeforeEach**: 
   - Creates ExtentTest for the current test execution

3. **During Test**: 
   - Call `logStepWithScreenshot()` after each important action

4. **@AfterAll**: 
   - Flushes ExtentReports to write all data to the HTML file

## Error Handling

If screenshot capture fails:
- Error is logged to SLF4J logger
- ExtentReports logs a WARNING status with the error message
- Test execution continues (doesn't fail the test)

## Dependencies Added

```xml
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <version>5.1.1</version>
    <scope>test</scope>
</dependency>
```

## Viewing Reports

1. Navigate to the extent-reports directory
2. Open the HTML file in any web browser
3. View test execution details, timestamps, and screenshots
4. Click on screenshots to view them in full size

## Best Practices

1. **Log meaningful steps**: Use descriptive step names that explain what's happening
2. **Capture after actions**: Call after each significant user action or assertion
3. **Keep it balanced**: Don't over-capture (too many screenshots can slow tests)
4. **Use consistent naming**: Follow a pattern like "Step X: Action description"

## Troubleshooting

### Report not generated
- Check that `extentReports.flush()` is called in `@AfterAll`
- Verify directory permissions for writing

### Screenshots not appearing
- Ensure JavaFX scene is fully rendered before capture
- Check for any exceptions in the logs

### Report file locked
- Close any browsers viewing the report before running tests again
- Reports have unique timestamps to avoid conflicts
