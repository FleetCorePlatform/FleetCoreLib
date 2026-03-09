package io.fleetcoreplatform.Builders;

import io.fleetcoreplatform.Models.MissionFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructs valid QGroundControl-compatible mission plans (.plan files). Employs a fluent builder
 * pattern to incrementally stack flight commands, waypoints, and parameter adjustments.
 */
public class MissionPlanBuilder {
    private final List<MissionFile.Item> missionItems;
    private int doJumpId = 1;
    private int hoverSpeed;
    private int cruiseSpeed;
    private double[] homePosition;

    private MissionPlanBuilder(int hoverSpeed, int cruiseSpeed) {
        this.hoverSpeed = hoverSpeed;
        this.cruiseSpeed = cruiseSpeed;
        this.missionItems = new ArrayList<>();
    }

    /**
     * Initializes a new builder with default operating speeds. Starts with a hover speed of 5 m/s
     * and a cruise speed of 15 m/s.
     *
     * @return A fresh {@link MissionPlanBuilder} instance.
     */
    public static MissionPlanBuilder builder() {
        return new MissionPlanBuilder(5, 15);
    }

    /**
     * Configures the transit speed used while navigating between waypoints.
     *
     * @param hoverSpeed Target speed in meters per second.
     * @return The current builder instance for chaining.
     */
    public MissionPlanBuilder hoverSpeed(int hoverSpeed) {
        this.hoverSpeed = hoverSpeed;
        return this;
    }

    /**
     * Configures the primary flight speed during mission execution.
     *
     * @param cruiseSpeed Target speed in meters per second.
     * @return The current builder instance for chaining.
     */
    public MissionPlanBuilder cruiseSpeed(int cruiseSpeed) {
        this.cruiseSpeed = cruiseSpeed;
        return this;
    }

    /**
     * Appends a new sequential command item to the mission plan. Wraps the raw MAVLink command
     * specifications into the JSON structure expected by PX4/ArduPilot planners.
     *
     * @param altitude Target altitude for the command, depending on the chosen altitude mode.
     * @param altitudeMode Determines the altitude frame of reference (e.g., MSL, AGL).
     * @param command The standard MAVLink command ID (e.g., 16 for WAYPOINT, 22 for TAKEOFF).
     * @param frame The coordinate frame applied to this item (e.g., 3 for GLOBAL_RELATIVE_ALT).
     * @param x The primary coordinate (longitude), or null if the command does not require spatial
     *     data.
     * @param y The secondary coordinate (latitude), or null if the command does not require spatial
     *     data.
     * @param z An additional parameter field, typically used for altitude overrides or auxiliary
     *     parameters.
     * @return The current builder instance for chaining.
     */
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

    /**
     * Defines the origin point for the mission. This dictates where the drone launches from and
     * where it will return upon a standard RTL (Return to Launch) command.
     *
     * @param x Longitude coordinate.
     * @param y Latitude coordinate.
     * @param z Altitude above mean sea level.
     * @return The current builder instance for chaining.
     */
    public MissionPlanBuilder homePosition(double x, double y, double z) {
        this.homePosition = new double[] {x, y, z};
        return this;
    }

    /**
     * Finalizes the mission configuration and packages all components into a compliant layout.
     * Populates empty geofence and rally point objects to ensure schema compliance for standard
     * ground stations.
     *
     * @return A fully populated {@link MissionFile} ready for JSON serialization.
     */
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
