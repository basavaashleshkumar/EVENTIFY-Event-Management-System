package service;

import dao.ReportDAO;
import dao.ReportDAO.ReportResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

/**
 * Service managing Reporting and Analytical Exports.
 */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public Map<String, Object> getDashboardStatistics() throws SQLException {
        return reportDAO.getDashboardStats();
    }

    public ReportResult getReport(int reportTypeIndex, int eventId) throws SQLException {
        switch (reportTypeIndex) {
            case 1: return reportDAO.reportAllEvents();
            case 2: return reportDAO.reportUpcomingEvents();
            case 3: return reportDAO.reportCompletedEvents();
            case 4: return reportDAO.reportEventsByOrganizer();
            case 5: return reportDAO.reportEventsByVenue();
            case 6: return reportDAO.reportParticipantsInEvent(eventId);
            case 7: return reportDAO.reportRegistrationCountPerEvent();
            case 8: return reportDAO.reportEventsSortedByRegistrations();
            case 9: return reportDAO.reportAvailableVenues();
            case 10: return reportDAO.reportCancelledEvents();
            default: return reportDAO.reportAllEvents();
        }
    }

    /**
     * Exports a ReportResult to a standard CSV file format.
     */
    public void exportToCsv(ReportResult report, File destinationFile) throws IOException {
        if (report == null || report.getColumns() == null) {
            throw new IllegalArgumentException("No report data available to export.");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(destinationFile))) {
            // Write column headers
            for (int i = 0; i < report.getColumns().length; i++) {
                writer.print(escapeCsv(report.getColumns()[i]));
                if (i < report.getColumns().length - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            // Write rows
            for (Object[] row : report.getData()) {
                for (int i = 0; i < row.length; i++) {
                    String cellVal = row[i] != null ? row[i].toString() : "";
                    writer.print(escapeCsv(cellVal));
                    if (i < row.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
