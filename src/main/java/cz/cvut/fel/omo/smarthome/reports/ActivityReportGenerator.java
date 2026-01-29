package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.ActivityEntry;
import cz.cvut.fel.omo.smarthome.logs.ActivityLog;
import java.time.format.DateTimeFormatter;

public class ActivityReportGenerator extends AbstractReportGenerator {

    private final ActivityLog log;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ActivityReportGenerator(ActivityLog log) {
        this.log = log;
    }

    @Override
    public void generate(String outputPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ACTIVITY REPORT ===\n\n");

        for (ActivityEntry e : log.getEntries()) {
            sb.append(String.format("%s | %s | %s | %s%n",
                    e.getTime().format(FMT),
                    e.getPersonName(),
                    e.getAction(),
                    e.getTargetName()));
        }

        // Вызываем метод родителя
        writeToFile(outputPath, sb.toString());
    }
}