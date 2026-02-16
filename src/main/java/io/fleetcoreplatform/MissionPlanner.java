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
import org.postgis.Geometry;
import org.postgis.Point;

public class MissionPlanner {
    /**
     * @param polygon The polygon to calculate the mission on
     * @param droneIdentities An array of DroneIdentities containing the drone name, and it's home
     *     position
     * @param altitude The altitude to run the mission at
     * @return A zip bundle containing the plans for each drone specified in <b>droneIdentities</b>
     * @throws IOException If there was an exception while building the zip file
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
}
