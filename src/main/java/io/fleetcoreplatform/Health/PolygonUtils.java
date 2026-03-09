package io.fleetcoreplatform.Health;

import io.fleetcoreplatform.Models.PolygonBoundingBox;
import io.fleetcoreplatform.Models.XYArrays;
import java.util.Arrays;
import org.postgis.Geometry;
import org.postgis.Point;

/** Utility functions for spatial operations and geometric calculations on PostGIS geometries. */
public class PolygonUtils {

    /**
     * Computes the dot product of two arrays representing vector components.
     *
     * @param a The first array of components.
     * @param b The second array of components.
     * @return The scalar dot product.
     */
    public static double dot(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }

    /**
     * Performs a circular right-shift on the elements of a double array. The last element wraps
     * around to become the first element.
     *
     * @param arr The array to roll.
     * @return A new array containing the shifted elements.
     */
    public static double[] roll(double[] arr) {
        int n = arr.length;
        double[] out = new double[n];
        out[0] = arr[n - 1];

        System.arraycopy(arr, 0, out, 1, n - 1);
        return out;
    }

    /**
     * Deconstructs a geometry into separate coordinate arrays for X and Y components. Useful for
     * vectorized math operations like Shoelace formula area calculations.
     *
     * @param polygon The source geometry to unpack.
     * @return An {@link XYArrays} object containing the isolated X and Y arrays.
     */
    public static XYArrays extractXY(Geometry polygon) {
        int n = polygon.numPoints();

        double[] xs = new double[n];
        double[] ys = new double[n];

        for (int i = 0; i < n; i++) {
            Point p = polygon.getPoint(i);
            xs[i] = p.x;
            ys[i] = p.y;
        }

        return new XYArrays(xs, ys);
    }

    /**
     * Computes the absolute enclosed area of a polygon using the Shoelace formula. Returns 0 for
     * degenerate polygons (less than 3 vertices).
     *
     * @param polygon The geometry to measure.
     * @return The calculated area in square coordinate units.
     */
    public static double calculatePolygonArea(Geometry polygon) {
        int n = polygon.numPoints();

        if (n < 3) {
            return 0f;
        }

        XYArrays arr = extractXY(polygon);
        var xs = arr.xs();
        var ys = arr.ys();

        return 0.5 * Math.abs(dot(xs, roll(ys)) - dot(ys, roll(xs)));
    }

    /**
     * Determines the tightest axis-aligned bounding box that fully encompasses the given polygon.
     *
     * @param polygon The geometry to bound.
     * @return A {@link PolygonBoundingBox} representing the extents.
     * @throws java.util.NoSuchElementException If the polygon contains no vertices.
     */
    public static PolygonBoundingBox getBoundingBox(Geometry polygon) {
        XYArrays arr = extractXY(polygon);
        var xs = arr.xs();
        var ys = arr.ys();

        double minX = Arrays.stream(xs).min().orElseThrow();
        double maxX = Arrays.stream(xs).max().orElseThrow();
        double minY = Arrays.stream(ys).min().orElseThrow();
        double maxY = Arrays.stream(ys).max().orElseThrow();

        return new PolygonBoundingBox(minX, maxX, minY, maxY);
    }
}
