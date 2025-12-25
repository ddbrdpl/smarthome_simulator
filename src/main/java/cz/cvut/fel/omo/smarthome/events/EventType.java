package cz.cvut.fel.omo.smarthome.events;

/**
 * Enumeration of all supported event types in the smart home system.
 *
 * <p>Each value represents a specific situation detected by the system
 * or generated during simulation.</p>
 */
public enum EventType {

    /** Water leakage detected */
    WATER_LEAK,

    /** Smoke or gas alert detected */
    SMOKE_ALERT,

    /** Motion detected by a sensor */
    MOTION_DETECTED,

    /** Device malfunction detected */
    DEVICE_BROKEN,

    /** Pool-related alert */
    POOL_ALERT,

    /** Pet has been outside for too long */
    PET_OUTSIDE_LONG,

    /** Device has been repaired */
    DEVICE_REPAIRED
}
