package cz.cvut.fel.omo.smarthome.consumption;

public class ConsumptionProfile {
    private final double powerW;       // Watts
    private final double waterLPerHour; // Liters/hour
    private final double gasM3PerHour;  // m3/hour

    public ConsumptionProfile(double powerW, double waterLPerHour, double gasM3PerHour) {
        this.powerW = powerW;
        this.waterLPerHour = waterLPerHour;
        this.gasM3PerHour = gasM3PerHour;
    }

    public double getPowerW() { return powerW; }
    public double getWaterLPerHour() { return waterLPerHour; }
    public double getGasM3PerHour() { return gasM3PerHour; }
}