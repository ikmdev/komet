package dev.ikm.komet.app.test.integration.testfx.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.service.support.CaptureSupport;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Utility class for test reporting and screenshots.
 */
public class TestReporter {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestReporter.class);
    
    private final ExtentReports extentReports;
    private ExtentTest extentTest;
    private final Path screenshotDirectory;
    private final FxRobot robot;
    
    public TestReporter(Path screenshotDirectory, Path reportDirectory, FxRobot robot) {
        this(screenshotDirectory, reportDirectory, robot, "KometWorkflowTest");
    }
    
    public TestReporter(Path screenshotDirectory, Path reportDirectory, FxRobot robot, String testSuiteName) {
        this.screenshotDirectory = screenshotDirectory;
        this.robot = robot;
        this.extentReports = initializeExtentReports(reportDirectory, testSuiteName);
    }
    
    private ExtentReports initializeExtentReports(Path reportDirectory, String testSuiteName) {
        try {
            // Ensure the report directory exists
            if (!java.nio.file.Files.exists(reportDirectory)) {
                java.nio.file.Files.createDirectories(reportDirectory);
                LOG.info("Created report directory: {}", reportDirectory);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportFileName = String.format("%s_%s.html", testSuiteName, timestamp);
            Path reportPath = reportDirectory.resolve(reportFileName);
            
            LOG.info("Initializing ExtentReports at: {}", reportPath.toAbsolutePath());
            
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath.toString());
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("Komet User Workflow Test Report");
            sparkReporter.config().setReportName("Komet Automated Test Execution");
            sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
            
            ExtentReports reports = new ExtentReports();
            reports.attachReporter(sparkReporter);
            reports.setSystemInfo("Application", "Komet");
            reports.setSystemInfo("Environment", "Test");
            reports.setSystemInfo("User", System.getProperty("user.name"));
            reports.setSystemInfo("Java Version", System.getProperty("java.version"));
            
            LOG.info("ExtentReports successfully initialized at: {}", reportPath.toAbsolutePath());
            return reports;
        } catch (Exception e) {
            LOG.error("Failed to initialize ExtentReports", e);
            throw new RuntimeException("Failed to initialize ExtentReports", e);
        }
    }
    
    public void createTest(String testName, String description) {
        if (extentReports != null) {
            extentTest = extentReports.createTest(testName, description);
            LOG.info("Created ExtentTest: {} - {}", testName, description);
        } else {
            LOG.error("Cannot create test - extentReports is null");
        }
    }
    
    public void logStepWithScreenshot(String stepDescription) {
        logStepWithScreenshot(stepDescription, Status.PASS);
    }
    
    public void logStepWithScreenshot(String stepDescription, Status status) {
        if (extentTest == null) {
            LOG.error("Cannot log step - extentTest is null. Was createTest() called?");
            return;
        }
        
        try {
            CaptureSupport captureSupport = FxService.serviceContext().getCaptureSupport();
            Image fxImage = captureSupport.captureNode(robot.targetWindow().getScene().getRoot());
            
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            extentTest.log(status, stepDescription,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image).build());
            
            LOG.info("Logged step with screenshot ({}): {}", status, stepDescription);
        } catch (Exception e) {
            LOG.error("Failed to capture screenshot for step: " + stepDescription, e);
            if (extentTest != null) {
                extentTest.log(Status.WARNING, stepDescription + " - Screenshot capture failed: " + e.getMessage());
            }
        }
    }
    
    public void logBeforeStep(String stepDescription) {
        logStepWithScreenshot("BEFORE: " + stepDescription, Status.INFO);
    }
    
    public void logAfterStep(String stepDescription) {
        logStepWithScreenshot("AFTER: " + stepDescription, Status.PASS);
    }
    
    public void logFailure(String stepDescription, Throwable error) {
        if (extentTest == null) {
            LOG.error("Cannot log failure - extentTest is null. Was createTest() called?");
            return;
        }
        
        try {
            CaptureSupport captureSupport = FxService.serviceContext().getCaptureSupport();
            Image fxImage = captureSupport.captureNode(robot.targetWindow().getScene().getRoot());
            
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            String errorMessage = String.format("FAILURE at %s: %s", stepDescription, error.getMessage());
            extentTest.log(Status.FAIL, errorMessage,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image).build());
            
            // Also save the screenshot to file
            try {
                if (!java.nio.file.Files.exists(screenshotDirectory)) {
                    java.nio.file.Files.createDirectories(screenshotDirectory);
                    LOG.info("Created screenshot directory: {}", screenshotDirectory);
                }
                
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = String.format("FAILURE_%s_%s.png", stepDescription.replaceAll("[^a-zA-Z0-9-_]", "_"), timestamp);
                Path screenshotPath = screenshotDirectory.resolve(filename);
                
                java.nio.file.Files.write(screenshotPath, imageBytes);
                LOG.info("Failure screenshot saved to file: {}", screenshotPath);
            } catch (Exception fileException) {
                LOG.error("Failed to save failure screenshot to file", fileException);
            }
            
            LOG.error("Logged failure with screenshot: {}", stepDescription, error);
        } catch (Exception e) {
            LOG.error("Failed to capture screenshot for failure at step: " + stepDescription, e);
            if (extentTest != null) {
                extentTest.log(Status.FAIL, stepDescription + " - Failed: " + error.getMessage() + 
                        " (Screenshot capture also failed: " + e.getMessage() + ")");
            }
        }
    }
    
    public void saveScreenshot(String name) {
        try {
            // Ensure screenshot directory exists
            if (!java.nio.file.Files.exists(screenshotDirectory)) {
                java.nio.file.Files.createDirectories(screenshotDirectory);
                LOG.info("Created screenshot directory: {}", screenshotDirectory);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s.png", name, timestamp);
            Path screenshotPath = screenshotDirectory.resolve(filename);
            
            CaptureSupport captureSupport = FxService.serviceContext().getCaptureSupport();
            Image image = captureSupport.captureNode(robot.targetWindow().getScene().getRoot());
            captureSupport.saveImage(image, screenshotPath);
            
            LOG.info("Screenshot saved: {}", screenshotPath);
        } catch (Exception e) {
            LOG.error("Failed to save screenshot", e);
        }
    }
    
    public void flush() {
        if (extentReports != null) {
            try {
                extentReports.flush();
                LOG.info("ExtentReports flushed successfully");
            } catch (Exception e) {
                LOG.error("Failed to flush ExtentReports", e);
            }
        } else {
            LOG.warn("ExtentReports is null, cannot flush");
        }
    }
}
