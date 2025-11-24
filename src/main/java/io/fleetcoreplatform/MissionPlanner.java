package io.fleetcoreplatform;

import io.fleetcoreplatform.Builders.MissionZipBuilder;
import io.fleetcoreplatform.Models.MissionFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.postgis.Geometry;

public class MissionPlanner {
    // TODO: Integrate the modules to complete the mission building process
    public static File buildMission(Geometry polygon) throws IOException {

        MissionFile missionFile =
                new MissionFile(
                        new MissionFile.GeoFence(new Object[0], new Object[0]),
                        new MissionFile.Mission(
                                15,
                                5,
                                new MissionFile.Item[] {
                                    new MissionFile.Item(
                                            null,
                                            0,
                                            0,
                                            true,
                                            530,
                                            1,
                                            2,
                                            new Double[] {0.0, 2.0, null, null, null, null, null}),
                                    new MissionFile.Item(
                                            null,
                                            6,
                                            1,
                                            true,
                                            22,
                                            2,
                                            3,
                                            new Double[] {
                                                0.0, 0.0, 0.0, null, 47.3977527, 8.5456078, 6.108192
                                            }),
                                    new MissionFile.Item(
                                            null,
                                            6,
                                            1,
                                            true,
                                            16,
                                            3,
                                            3,
                                            new Double[] {
                                                0.0,
                                                0.0,
                                                0.0,
                                                null,
                                                47.39777142,
                                                8.54566905,
                                                6.108192
                                            }),
                                    new MissionFile.Item(
                                            null,
                                            6,
                                            1,
                                            true,
                                            16,
                                            4,
                                            3,
                                            new Double[] {
                                                0.0,
                                                0.0,
                                                0.0,
                                                null,
                                                47.39779004,
                                                8.54558785,
                                                6.108192
                                            }),
                                    new MissionFile.Item(
                                            null,
                                            0,
                                            0,
                                            true,
                                            20,
                                            5,
                                            2,
                                            new Double[] {
                                                0.0, 0.0, 0.0, 0.0, 47.3977527, 8.5456078, 0.0
                                            })
                                },
                                new double[] {47.3977527, 8.5456078, 491}),
                        "QGroundControl",
                        new MissionFile.RallyPoints(new Object[0]));

        try (MissionZipBuilder builder = new MissionZipBuilder(UUID.randomUUID().toString())) {
            try (InputStream stream1 = missionFile.toStream()) {
                builder.mission("test1", stream1);
            }
            try (InputStream stream2 = missionFile.toStream()) {
                builder.mission("test2", stream2);
            }
            return builder.build();
        }
    }
}
