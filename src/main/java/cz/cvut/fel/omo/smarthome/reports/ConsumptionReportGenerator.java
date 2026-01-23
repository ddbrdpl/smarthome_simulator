package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class ConsumptionReportGenerator {

    private final ConsumptionLog log;

    public ConsumptionReportGenerator(ConsumptionLog log) {
        this.log = log;
    }

    public void generate(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONSUMPTION REPORT ===\n\n");
        sb.append("Device | Power (kWh) | Water (L) | Gas (m3)\n");
        sb.append("------------------------------------------\n");

        log.getRecordsByDeviceId().values().stream()
                .sorted(Comparator.comparing(ConsumptionRecord::getDeviceName))
                .forEach(r -> sb.append(String.format("%s | %.4f | %.2f | %.4f%n",
                        r.getDeviceName(), r.getPowerKWh(), r.getWaterL(), r.getGasM3())));

        try {
            Path p = Path.of(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write consumption report", e);
        }
    }
}