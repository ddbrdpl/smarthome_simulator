package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.EventEntry;
import cz.cvut.fel.omo.smarthome.logs.EventLog;

public class EventReportGenerator extends AbstractReportGenerator {

    private final EventLog log;

    public EventReportGenerator(EventLog log) {
        this.log = log;
    }

    @Override
    public void generate(String outputPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EVENT REPORT ===\n\n");

        for (EventEntry e : log.getEntries()) {
            sb.append(e.getTime()).append(" | ").append(e.getType()).append(" | ");

            if (e.getDeviceName() != null) sb.append("Dev: ").append(e.getDeviceName()).append(" | ");
            if (e.getCausedBy() != null) sb.append("Caused: ").append(e.getCausedBy()).append(" | ");

            sb.append("Handled: ").append(e.getHandledBy()).append("\n");
        }

        writeToFile(outputPath, sb.toString());
    }
}