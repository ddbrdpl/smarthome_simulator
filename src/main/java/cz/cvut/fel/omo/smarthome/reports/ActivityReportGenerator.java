package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter; // Import added

public class ActivityReportGenerator {

    private final ActivityLog log;
    // Formatter for clean timestamps
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ActivityReportGenerator(ActivityLog log) {
        this.log = log;
    }

    public void generate(String outputPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ACTIVITY REPORT ===\n\n");

        for (ActivityEntry e : log.getEntries()) {
            // Use formatter here
            sb.append(String.format("%s | %s | %s | %s%n",
                    e.getTime().format(FMT),
                    e.getPersonName(),
                    e.getAction(),
                    e.getTargetName()));
        }

        writeToFile(outputPath, sb.toString());
    }

    private void writeToFile(String path, String content) {
        try {
            Path p = Path.of(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.writeString(p, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report: " + path, e);
        }
    }
}