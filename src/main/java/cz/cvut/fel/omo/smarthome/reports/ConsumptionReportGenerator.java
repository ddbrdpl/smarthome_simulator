package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Generates a textual report summarizing resource consumption
 * of all devices in the smart home.
 *
 * <p>The report includes, for each device:</p>
 * <ul>
 *   <li>Total electricity consumption (kWh)</li>
 *   <li>Total water consumption (liters)</li>
 *   <li>Total gas consumption (m³)</li>
 * </ul>
 *
 * <p>Devices are sorted alphabetically by name to improve readability.</p>
 */
public class ConsumptionReportGenerator {

    /** Source consumption log containing aggregated data. */
    private final ConsumptionLog log;

    /**
     * Creates a new consumption report generator.
     *
     * @param log consumption log to read from
     */
    public ConsumptionReportGenerator(ConsumptionLog log) {
        this.log = log;
    }

    /**
     * Writes the consumption report into a text file.
     *
     * @param path target output path (directories are created automatically)
     * @throws IllegalStateException if the report cannot be written
     */
    public void generate(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONSUMPTION REPORT ===\n\n");
        sb.append("Device | Power (kWh) | Water (L) | Gas (m3)\n");
        sb.append("------------------------------------------\n");

        log.getRecordsByDeviceId().values().stream()
                .sorted(Comparator.comparing(ConsumptionRecord::getDeviceName))
                .forEach(r -> sb.append(r.getDeviceName()).append(" | ")
                        .append(String.format("%.4f", r.getPowerKWh())).append(" | ")
                        .append(String.format("%.2f", r.getWaterL())).append(" | ")
                        .append(String.format("%.4f", r.getGasM3()))
                        .append("\n"));

        try {
            Path p = Path.of(path);
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.writeString(p, sb.toString());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write consumption report", ex);
        }
    }
}
