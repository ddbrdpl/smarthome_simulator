package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.consumption.ConsumptionLog;

public class ConsumptionReportGenerator extends AbstractReportGenerator {

    private final SmartHomeContext ctx;

    // Цены
    private static final double PRICE_PER_KWH = 6.50;
    private static final double PRICE_PER_LITER = 0.10;
    private static final double PRICE_PER_GAS = 20.0;

    // Передаем контекст в конструктор
    public ConsumptionReportGenerator(SmartHomeContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void generate(String outputPath) {
        ConsumptionLog log = ctx.getConsumptionLog();
        StringBuilder sb = new StringBuilder();

        sb.append("=== CONSUMPTION & BILLING REPORT ===\n\n");
        sb.append(String.format("%-25s | %-12s | %-10s | %-10s | %-10s%n",
                "Device", "Power (kWh)", "Water (L)", "Gas (m3)", "Cost (CZK)"));
        sb.append("-".repeat(75)).append("\n");

        double totalPower = 0;
        double totalWater = 0;
        double totalGas = 0;
        double totalCost = 0;

        for (Device d : ctx.getAllDevices()) {
            double p = log.getPower(d);
            double w = log.getWater(d);
            double g = log.getGas(d);

            if (p == 0 && w == 0 && g == 0) continue;

            double deviceCost = (p * PRICE_PER_KWH) + (w * PRICE_PER_LITER) + (g * PRICE_PER_GAS);

            sb.append(String.format("%-25s | %-12.4f | %-10.2f | %-10.4f | %-10.2f%n",
                    d.getName(), p, w, g, deviceCost));

            totalPower += p;
            totalWater += w;
            totalGas += g;
            totalCost += deviceCost;
        }

        sb.append("-".repeat(75)).append("\n");
        sb.append(String.format("%-25s | %-12.4f | %-10.2f | %-10.4f | %-10.2f%n",
                "TOTALS", totalPower, totalWater, totalGas, totalCost));

        writeToFile(outputPath, sb.toString());
    }
}