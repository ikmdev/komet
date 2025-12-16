package dev.ikm.komet.layout.orchestration;

/**
 * The StatusReportService interface defines a method for reporting status of an activity to the user.
 */
public interface StatusReportService {
    /**
     * Reports the status of an activity to the user.
     *
     * @param status The status message to report.
     */
    void reportStatus(String status);
}
