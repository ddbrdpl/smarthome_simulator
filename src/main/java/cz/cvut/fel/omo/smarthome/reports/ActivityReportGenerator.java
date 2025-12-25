package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a textual activity report from {@link ActivityLog}.
 *
 * <p>The report contains a chronological list of user/system actions, formatted as:</p>
 * <pre>
 * timestamp | personName | action | targetName
 * </pre>
 *
 * <p>This report is used to demonstrate behavior of people and the system,
 * including permission denials and automatic purchases.</p>
 */
public class ActivityReportGenerator {

    /** Source activity log. */
    private final ActivityLog log;

    /**
     * Creates a new activity report generator.
     *
     * @param log activity log to read from
     */
    public ActivityReportGenerator(ActivityLog log) {
        this.log = log;
    }

    /**
     * Writes the activity report into a text file.
     *
     * @param outputPath target output path (directories are created automatically)
     * @throws IllegalStateException if the report cannot be written
     */
    public void generate(String outputPath) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== ACTIVITY REPORT ===\n\n");

        for (ActivityEntry e : log.getEntries()) {
            sb.append(e.getTime()).append(" | ")
                    .append(e.getPersonName()).append(" | ")
                    .append(e.getAction()).append(" | ")
                    .append(e.getTargetName()).append("\n");
        }

        try {
            Path p = Path.of(outputPath);
            Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write activity report", ex);
        }
    }
}
