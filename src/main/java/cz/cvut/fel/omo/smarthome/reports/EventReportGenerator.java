package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.EventEntry;
import cz.cvut.fel.omo.smarthome.logs.EventLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EventReportGenerator {

    private final EventLog log;

    public EventReportGenerator(EventLog log) {
        this.log = log;
    }

    public void generate(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EVENT REPORT ===\n\n");

        for (EventEntry e : log.getEntries()) {
            sb.append(e.getTime()).append(" | ")
                    .append(e.getType()).append(" | ");

            if (e.getDeviceName() != null) {
                sb.append("device: ").append(e.getDeviceName()).append(" | ");
            }

            if (e.getCausedBy() != null) {
                sb.append("caused by: ").append(e.getCausedBy()).append(" | ");
            }

            sb.append("handled by: ").append(e.getHandledBy())
                    .append("\n");
        }

        try {
            Path p = Path.of(path);
            Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write event report", ex);
        }
    }
}
