package cz.cvut.fel.omo.smarthome.reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractReportGenerator implements ReportGenerator {
    protected void writeToFile(String path, String content) {
        try {
            Path p = Path.of(path);
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.writeString(p, content);
            System.out.println("Report generated: " + path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to: " + path, e);
        }
    }
}