package io.fleetcoreplatform.Models;

/**
 * Identifies a drone asset and its associated launch origin for a specific mission deployment.
 *
 * @param name A human-readable identifier or callsign for the drone.
 * @param home The designated return-to-launch (RTL) coordinates for this unit.
 */
public record DroneIdentity(String name, Home home) {

    /**
     * Represents the absolute 3D spatial origin point assigned to a drone.
     *
     * @param x The longitude coordinate.
     * @param y The latitude coordinate.
     * @param z The altitude, typically expressed as MSL (Mean Sea Level).
     */
    public record Home(double x, double y, double z) {}
}
