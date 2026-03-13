package io.fleetcoreplatform;

import io.fleetcoreplatform.Algorithms.MowerSurveyAlgorithm;
import io.fleetcoreplatform.Algorithms.PolygonPartitioner;
import io.fleetcoreplatform.Builders.MissionPlanBuilder;
import io.fleetcoreplatform.Builders.MissionZipBuilder;
import io.fleetcoreplatform.Models.DroneIdentity;
import io.fleetcoreplatform.Models.MissionFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import io.fleetcoreplatform.Models.PolygonPoint2D;
import org.postgis.Geometry;
import org.postgis.Point;

/**
 * Top-level orchestrator for mapping and translating an overarching area survey into discrete drone
 * assignments.
 */
public class MissionPlanner {

    /**
     * Slices an input geographical area into distinct sub-regions, generates a sweeping path for
     * each section, and bundles the individual drone plans into a consolidated archive payload. The
     * method calculates required recursion depths to satisfy the number of active drones provided.
     *
     * @param polygon The global boundary polygon outlining the entire survey operation.
     * @param droneIdentities The hardware fleet list providing callsigns and initial launch
     *     coordinates.
     * @param altitude The strict cruising altitude applied to all generated flight trajectories.
     * @return A zipped package containing localized `.plan` files assigned to specific hardware
     *     UUIDs.
     * @throws IOException If the underlying filesystem fails to generate or write the bundled zip.
     */
    public static File buildMission(Geometry polygon, DroneIdentity[] droneIdentities, int altitude)
            throws IOException {
        try (MissionZipBuilder zipBuilder = new MissionZipBuilder(UUID.randomUUID().toString())) {
            int recursionDepth = (int) Math.floor(Math.log(droneIdentities.length) / Math.log(2));

            Geometry[] partitions =
                    PolygonPartitioner.bisectPolygon(
                            polygon,
                            0,
                            recursionDepth,
                            0.01); // Min area needs testing, and adjustments

            for (int i = 0; i < droneIdentities.length; i++) {
                DroneIdentity.Home homePos = droneIdentities[i].home();

                MissionPlanBuilder planBuilder =
                        MissionPlanBuilder.builder()
                                .cruiseSpeed(15)
                                .hoverSpeed(5)
                                .homePosition(homePos.x(), homePos.y(), homePos.z());

                Geometry assignedPartition = partitions[i % partitions.length];

                Point[] points = MowerSurveyAlgorithm.calculatePath(assignedPartition, 1.5, true);

                planBuilder.item(
                        0, 0, 530, 2, null, null,
                        null); // MAV_CMD_SET_CAMERA_MODE - Set camera running mode

                for (int j = 0; j < points.length; j++) {
                    double x = points[j].x;
                    double y = points[j].y;
                    double z = points[j].z;

                    if (j == 0) {
                        planBuilder.item(altitude, 1, 22, 3, x, y, z);
                    } else if (j == points.length - 1) {
                        planBuilder.item(0, 0, 20, 2, x, y, z);
                    } else {
                        planBuilder.item(altitude, 1, 16, 3, x, y, z);
                    }
                }

                MissionFile plan = planBuilder.build();
                try (InputStream stream = plan.toStream()) {
                    zipBuilder.mission(droneIdentities[i].name(), stream);
                }
            }

            return zipBuilder.build();
        }
    }

    public static File buildManualMission(PolygonPoint2D[] waypoints, DroneIdentity drone, int altitude, int speed, boolean rtl) throws IOException {
    try (MissionZipBuilder zipBuilder = new MissionZipBuilder(UUID.randomUUID().toString())) {
        DroneIdentity.Home homePos = drone.home();
        MissionPlanBuilder planBuilder = MissionPlanBuilder.builder()
                .cruiseSpeed(speed)
                .hoverSpeed(5)
                .homePosition(homePos.x(), homePos.y(), homePos.z());

        planBuilder.item(0, 0, 530, 2, null, null, null);

        for (int i = 0; i < waypoints.length; i++) {
            double x = waypoints[i].x();
            double y = waypoints[i].y();

            if (i == 0) {
                planBuilder.item(altitude, 1, 22, 3, x, y, (double) altitude);
            } else if (i == waypoints.length - 1) {
                if (rtl) {
                    planBuilder.item(0, 0, 20, 2, x, y, (double) altitude);
                } else {
                    planBuilder.item(0, 0, 21, 2, x, y, (double) altitude);
                }
            } else {
                planBuilder.item(altitude, 1, 16, 3, x, y, (double) altitude);
            }
        }

        MissionFile plan = planBuilder.build();
        try (InputStream stream = plan.toStream()) {
            zipBuilder.mission(drone.name(), stream);
        }

        return zipBuilder.build();
    }
}
}
