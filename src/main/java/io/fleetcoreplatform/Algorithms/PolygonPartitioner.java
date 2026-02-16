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

public class PolygonPartitioner {
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
     * Recursively bisects a polygon into smaller sub-polygons using binary space partitioning.
     * Splits along the longest axis (width or height) at each iteration until maxDepth is reached
     * or minimum area constraint is violated.
     *
     * @param polygon The polygon to perform the bisection on
     * @param depth Current recursion depth (start with 0)
     * @param maxDepth Maximum recursion depth - produces 2^maxDepth sub-polygons
     * @param minArea Minimum area threshold - partitions smaller than this are not split further
     * @return Array of sub-polygons resulting from the bisection process
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
