package cz.cvut.fel.omo.smarthome.people;

/**
 * Defines roles of residents in the smart home.
 *
 * <p>Roles are used for:</p>
 * <ul>
 *   <li>Permission/authorization checks</li>
 *   <li>Simulation behavior tuning (e.g., breakdown probability)</li>
 *   <li>Logging and reports</li>
 * </ul>
 */
public enum Role {
    FATHER,
    MOTHER,
    SON,
    DAUGHTER,
    GRANDFATHER,
    CAT
}
