package io.fleetcoreplatform.Models;

/**
 * Defines a rigid 2D Cartesian envelope wrapped around a complex geometry. Used primarily to
 * optimize spatial queries and set up bisection axes before precise boundary checks.
 */
public class PolygonBoundingBox {
    public double minX;
    public double maxX;
    public double minY;
    public double maxY;

    /**
     * Initializes the minimum and maximum extents.
     *
     * @param minX The leftmost X coordinate.
     * @param maxX The rightmost X coordinate.
     * @param minY The lowest Y coordinate.
     * @param maxY The highest Y coordinate.
     */
    public PolygonBoundingBox(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
}
