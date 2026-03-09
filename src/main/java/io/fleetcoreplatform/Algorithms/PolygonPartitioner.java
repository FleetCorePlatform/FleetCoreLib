package io.fleetcoreplatform.Algorithms;

import io.fleetcoreplatform.Health.PolygonUtils;
import io.fleetcoreplatform.Models.PolygonBoundingBox;
import io.fleetcoreplatform.Models.SplitPolygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.postgis.Geometry;
import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;

/**
 * Handles the subdivision of geographical areas into smaller, manageable chunks for drone swarms.
 * Uses a binary space partitioning (BSP) strategy to recursively divide a polygon into equal
 * halves.
 */
public class PolygonPartitioner {

    /**
     * Divides a given polygon into two distinct polygons along a specified axis. Uses the
     * Sutherland-Hodgman approach to calculate intersection points when the split line cuts across
     * an edge.
     *
     * @param polygon The geometry representing the area to split.
     * @param axis The axis to split across ('x' for longitude/horizontal, 'y' for
     *     latitude/vertical).
     * @param splitValue The coordinate value on the specified axis where the split line is drawn.
     * @return A {@link SplitPolygon} record containing the two newly formed sub-polygons.
     */
    private static SplitPolygon clipPolygon(Geometry polygon, char axis, double splitValue) {
        int n = polygon.numPoints();

        List<Point> left = new ArrayList<>();
        List<Point> right = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Point current = polygon.getPoint(i);
            Point next = polygon.getPoint((i + 1) % n);

            double currentValue = axis == 'x' ? current.x : current.y;
            double nextValue = axis == 'x' ? next.x : next.y;

            boolean currentInside = currentValue <= splitValue;
            boolean nextInside = nextValue <= splitValue;

            if (currentInside) {
                left.add(current);
            } else {
                right.add(current);
            }

            if (currentInside != nextInside) {
                double t = (splitValue - currentValue) / (nextValue - currentValue);

                double ix = current.x + t * (next.x - current.x);
                double iy = current.y + t * (next.y - current.y);
                Point intersection = new Point(ix, iy);

                left.add(intersection);
                right.add(intersection);
            }
        }

        Point[] leftArray = left.toArray(new Point[0]);
        Point[] rightArray = right.toArray(new Point[0]);

        Polygon leftPolygon = new Polygon(new LinearRing[] {new LinearRing(leftArray)});
        Polygon rightPolygon = new Polygon(new LinearRing[] {new LinearRing(rightArray)});

        return new SplitPolygon(leftPolygon, rightPolygon);
    }

    /**
     * Recursively partitions a polygon into multiple sub-polygons up to a specified depth. At each
     * step, it determines the bounding box's longest axis and bisects the polygon across its
     * center. Recursion stops early if a sub-polygon's area falls below the minimum threshold or if
     * it cannot form a valid shape.
     *
     * @param polygon The area geometry to partition.
     * @param depth The current zero-indexed recursion depth.
     * @param maxDepth The recursion limit. A value of N yields up to 2^N sub-polygons.
     * @param minArea The strict minimum area threshold. Regions below this size are kept intact
     *     without further splitting.
     * @return An array of the resulting partitioned geometries.
     */
    public static Geometry[] bisectPolygon(
            Geometry polygon, int depth, int maxDepth, double minArea) {
        double area = PolygonUtils.calculatePolygonArea(polygon);

        if (depth >= maxDepth || area < minArea || polygon.numPoints() < 3) {
            return new Geometry[] {polygon};
        }

        PolygonBoundingBox bbox = PolygonUtils.getBoundingBox(polygon);
        double width = bbox.maxX - bbox.minX;
        double height = bbox.maxY - bbox.minY;

        char axis = width > height ? 'x' : 'y';
        double splitValue = axis == 'x' ? (bbox.minX + bbox.maxX) / 2 : (bbox.minY + bbox.maxY) / 2;

        SplitPolygon splitPolygon = clipPolygon(polygon, axis, splitValue);

        ArrayList<Geometry> result = new ArrayList<>();

        if (splitPolygon.left().numPoints() >= 3) {
            result.addAll(
                    Arrays.asList(
                            bisectPolygon(splitPolygon.left(), depth + 1, maxDepth, minArea)));
        }
        if (splitPolygon.right().numPoints() >= 3) {
            result.addAll(
                    Arrays.asList(
                            bisectPolygon(splitPolygon.right(), depth + 1, maxDepth, minArea)));
        }

        return result.toArray(new Geometry[0]);
    }
}
