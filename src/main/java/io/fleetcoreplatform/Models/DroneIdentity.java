package io.fleetcoreplatform.Models;

public record DroneIdentity(String name, Home home) {
    public record Home(int x, int y, int z) {}
}
