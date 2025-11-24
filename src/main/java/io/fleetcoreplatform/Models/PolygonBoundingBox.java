package io.fleetcoreplatform.Models;

public class PolygonBoundingBox {
    public double minX;
    public double maxX;
    public double minY;
    public double maxY;

    public PolygonBoundingBox(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
}
