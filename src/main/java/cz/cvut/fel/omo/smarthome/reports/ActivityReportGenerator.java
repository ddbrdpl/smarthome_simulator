package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ActivityReportGenerator {

    private final ActivityLog log;

    public ActivityReportGenerator(ActivityLog log) {
        this.log = log;
    }

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
