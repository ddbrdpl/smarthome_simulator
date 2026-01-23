package cz.cvut.fel.omo.smarthome.consumption;

public class ConsumptionRecord {
    private final String deviceId;
    private final String deviceName;

    private double powerKWh;
    private double waterL;
    private double gasM3;

    public ConsumptionRecord(String deviceId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public void add(double addPowerKWh, double addWaterL, double addGasM3) {
        this.powerKWh += addPowerKWh;
        this.waterL += addWaterL;
        this.gasM3 += addGasM3;
    }

    public String getDeviceName() { return deviceName; }
    public double getPowerKWh() { return powerKWh; }
    public double getWaterL() { return waterL; }
    public double getGasM3() { return gasM3; }
}