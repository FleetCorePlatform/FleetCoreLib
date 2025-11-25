package io.fleetcoreplatform.Builders;

import io.fleetcoreplatform.Models.MissionFile;
import java.util.List;

public class MissionPlanBuilder {
    private List<MissionFile.Item> missionItems;
    private int doJumpId = 1;
    private int hoverSpeed;
    private int cruiseSpeed;
    private double[] homePosition;

    private MissionPlanBuilder(int hoverSpeed, int cruiseSpeed) {
        this.hoverSpeed = hoverSpeed;
        this.cruiseSpeed = cruiseSpeed;
    }

    public static MissionPlanBuilder builder() {
        return new MissionPlanBuilder(5, 15);
    }

    public MissionPlanBuilder hoverSpeed(int hoverSpeed) {
        this.hoverSpeed = hoverSpeed;
        return this;
    }

    public MissionPlanBuilder cruiseSpeed(int cruiseSpeed) {
        this.cruiseSpeed = cruiseSpeed;
        return this;
    }

    public MissionPlanBuilder item(
            int altitude, int altitudeMode, int command, int frame, Double x, Double y, Double z) {
        Double[] params = new Double[] {0.0, 0.0, 0.0, null, x, y, z};

        MissionFile.Item item =
                new MissionFile.Item(
                        null, altitude, altitudeMode, true, command, doJumpId, frame, params);

        doJumpId += 1;
        missionItems.add(item);

        return this;
    }

    public MissionPlanBuilder homePosition(double x, double y, double z) {
        this.homePosition = new double[] {x, y, z};
        return this;
    }

    public MissionFile build() {
        MissionFile.GeoFence geoFence = new MissionFile.GeoFence(new Object[0], new Object[0]);

        MissionFile.Item[] items = missionItems.toArray(new MissionFile.Item[0]);
        MissionFile.Mission mission =
                new MissionFile.Mission(
                        this.cruiseSpeed, this.hoverSpeed, items, this.homePosition);

        MissionFile.RallyPoints rallyPoints = new MissionFile.RallyPoints(new Object[0]);

        return new MissionFile(geoFence, mission, "FleetCoreServer", rallyPoints);
    }
}
