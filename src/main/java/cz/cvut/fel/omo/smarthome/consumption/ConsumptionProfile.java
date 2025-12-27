package cz.cvut.fel.omo.smarthome.consumption;

/**
 * Defines a simple per-hour resource consumption profile for a device.
 *
 * The profile specifies how much electricity, water, and gas
 * a device consumes when it is in the ON state.
 *
 * This model is intentionally simplified:
 * <ul>
 *   <li>Electricity is expressed in watts (W)</li>
 *   <li>Water is expressed in liters per hour (L/h)</li>
 *   <li>Gas is expressed in cubic meters per hour (m³/h)</li>
 * </ul>
 *
 *
 * he actual consumption is calculated by {@code Device.accumulateConsumption()}
 * based on the simulation step duration.
 */
public class ConsumptionProfile {

    /** Electricity consumption in watts (W) per hour of operation. */
    private final double powerW;

    /** Water consumption in liters per hour (L/h). */
    private final double waterLPerHour;

    /** Gas consumption in cubic meters per hour (m³/h). */
    private final double gasM3PerHour;

    /**
     * Creates a new consumption profile.
     *
     * @param powerW         electricity consumption in watts
     * @param waterLPerHour  water consumption in liters per hour
     * @param gasM3PerHour   gas consumption in cubic meters per hour
     */
    public ConsumptionProfile(double powerW, double waterLPerHour, double gasM3PerHour) {
        this.powerW = powerW;
        this.waterLPerHour = waterLPerHour;
        this.gasM3PerHour = gasM3PerHour;
    }

    /** @return electricity consumption in watts */
    public double getPowerW() { return powerW; }

    /** @return water consumption in liters per hour */
    public double getWaterLPerHour() { return waterLPerHour; }

    /** @return gas consumption in cubic meters per hour */
    public double getGasM3PerHour() { return gasM3PerHour; }
}
