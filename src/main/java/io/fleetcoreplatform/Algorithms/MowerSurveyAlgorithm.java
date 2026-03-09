package io.fleetcoreplatform.Algorithms;

import io.fleetcoreplatform.Health.PolygonUtils;
import io.fleetcoreplatform.Models.PolygonBoundingBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.postgis.Geometry;
import org.postgis.Point;

/**
 * Implements a sweep-line algorithm to generate boustrophedon (lawnmower) flight paths. These paths
 * are standard for comprehensive aerial surveying and mapping missions.
 */
public class MowerSurveyAlgorithm {

    /**
     * Calculates a zig-zag route over a specified polygon area. The algorithm runs horizontal sweep
     * lines from the bottom to the top of the polygon's bounding box, computing the intersection
     * segments and linking them together.
     *
     * @param polygon The surveyed region geometry.
     * @param spacing The vertical distance between parallel sweep lines (corresponds to the
     *     camera's ground footprint).
     * @param zigZag Defines the initial entry direction. If true, the first line goes
     *     right-to-left.
     * @return An ordered array of geographical points defining the continuous flight trajectory.
     */
    public static Point[] calculatePath(Geometry polygon, double spacing, boolean zigZag) {
        PolygonBoundingBox boundingBox = PolygonUtils.getBoundingBox(polygon);

        List<Double> sweepLinesY = new ArrayList<>();

        for (double y = boundingBox.minY; y <= boundingBox.maxY; y += spacing) {
            sweepLinesY.add(y);
        }

        List<Point> pathPoints = new ArrayList<>();
        for (Double y : sweepLinesY) {
            List<Double> intersections = new ArrayList<>();
            int n = polygon.numPoints();

            for (int i = 0; i < n; i++) {
                Point p1 = polygon.getPoint(i);
                Point p2 = polygon.getPoint((i + 1) % n);

                if ((p1.y <= y && y <= p2.y) || (p2.y <= y && y <= p1.y)) {
                    if (Math.abs(p1.y - p2.y) < 1e-9) {
                        continue;
                    } else {
                        double t = (y - p1.y) / (p2.y - p1.y);
                        double x = p1.x + t * (p2.x - p1.x);
                        intersections.add(x);
                    }
                }
            }

            if (intersections.isEmpty()) {
                continue;
            }

            Collections.sort(intersections);

            List<Double> uniqueIntersections = new ArrayList<>();
            uniqueIntersections.add(intersections.getFirst());
            for (int k = 1; k < intersections.size(); k++) {
                if (Math.abs(intersections.get(k) - uniqueIntersections.getLast()) > 1e-9) {
                    uniqueIntersections.add(intersections.get(k));
                }
            }

            for (int j = 0; j < uniqueIntersections.size() - 1; j += 2) {
                if (j + 1 >= uniqueIntersections.size()) {
                    break;
                }

                double startX = uniqueIntersections.get(j);
                double endX = uniqueIntersections.get(j + 1);

                List<Point> segmentPoints = new ArrayList<>();
                segmentPoints.add(new Point(startX, y));
                segmentPoints.add(new Point(endX, y));

                if (zigZag) {
                    Collections.reverse(segmentPoints);
                }

                pathPoints.addAll(segmentPoints);
                zigZag = !zigZag;
            }
        }

        return pathPoints.toArray(new Point[0]);
    }
}
