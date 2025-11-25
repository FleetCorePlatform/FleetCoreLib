package io.fleetcoreplatform.Models;

public record DroneIdentity(String name, Home home) {
    public record Home(double x, double y, double z) {}
}
