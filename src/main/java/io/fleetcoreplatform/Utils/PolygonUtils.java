package io.fleetcoreplatform.Utils;

import io.fleetcoreplatform.Models.PolygonBoundingBox;
import io.fleetcoreplatform.Models.XYArrays;
import java.util.Arrays;
import org.postgis.Geometry;
import org.postgis.Point;

public class PolygonUtils {
    public static double dot(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }

    public static double[] roll(double[] arr) {
        int n = arr.length;
        double[] out = new double[n];
        out[0] = arr[n - 1];

        System.arraycopy(arr, 0, out, 1, n - 1);
        return out;
    }

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
