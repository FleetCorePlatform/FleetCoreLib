package io.fleetcoreplatform.Algorithms;

import io.fleetcoreplatform.Models.PolygonBoundingBox;
import io.fleetcoreplatform.Utils.PolygonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.postgis.Geometry;
import org.postgis.Point;

public class MowerSurveyAlgorithm {
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
                    if (p1.y == p2.y) {
                        intersections.add(p1.x);
                        intersections.add(p2.x);
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
            for (int j = 0; j < intersections.size(); j += 2) {
                double startX = intersections.get(j);
                double endX = intersections.get(j + 1);

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
