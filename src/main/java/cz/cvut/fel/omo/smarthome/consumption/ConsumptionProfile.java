package cz.cvut.fel.omo.smarthome.consumption;

public class ConsumptionProfile {

    // per hour (simple model)
    private final double powerW;       // electricity (W)
    private final double waterLPerHour; // water (L/h)
    private final double gasM3PerHour;  // gas (m^3/h)

    public ConsumptionProfile(double powerW, double waterLPerHour, double gasM3PerHour) {
        this.powerW = powerW;
        this.waterLPerHour = waterLPerHour;
        this.gasM3PerHour = gasM3PerHour;
    }

    public double getPowerW() { return powerW; }
    public double getWaterLPerHour() { return waterLPerHour; }
    public double getGasM3PerHour() { return gasM3PerHour; }
}
