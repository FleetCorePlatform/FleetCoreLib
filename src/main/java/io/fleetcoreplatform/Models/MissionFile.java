package io.fleetcoreplatform.Models;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Acts as the structured representation of a standard drone `.plan` file. Compatible with
 * QGroundControl mission schemas, defining flight items, geofences, and safety parameters.
 */
public class MissionFile {
    public final String fileType = "Plan";
    public GeoFence geoFence;
    public String groundStation;
    public Mission mission;
    public RallyPoints rallyPoints;
    public final int version = 1;

    /**
     * Initializes the root mission container.
     *
     * @param geoFence The bound area within which the drone is restricted.
     * @param mission The core sequential flight plan structure.
     * @param groundStation The software client producing the file (e.g., "FleetCoreServer").
     * @param rallyPoints Defined fallback locations for emergencies.
     */
    public MissionFile(
            GeoFence geoFence, Mission mission, String groundStation, RallyPoints rallyPoints) {
        this.geoFence = geoFence;
        this.mission = mission;
        this.groundStation = groundStation;
        this.rallyPoints = rallyPoints;
    }

    /**
     * Serializes the object model into a standard JSON byte stream for file writing.
     *
     * @return A valid UTF-8 input stream comprising the JSON payload.
     */
    public InputStream toStream() {
        String json = new Gson().toJson(this);
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    /** Contains emergency return coordinates that bypass standard RTL behavior if triggered. */
    public static class RallyPoints {
        public Object[] points;
        public final int version = 2;

        public RallyPoints(Object[] points) {
            this.points = points;
        }
    }

    /**
     * Maps explicit restricted bounds, utilizing shapes like circular radii or standard polygons.
     */
    public static class GeoFence {
        public Object[] circles;
        public Object[] polygons;
        public final int version = 2;

        public GeoFence(Object[] circles, Object[] polygons) {
            this.circles = circles;
            this.polygons = polygons;
        }
    }

    /**
     * Encapsulates the runtime variables assigned for the overarching flight routine, linking
     * specific waypoints.
     */
    public static class Mission {
        public int cruiseSpeed;
        public final int firmwareType = 12;
        public final int globalPlanAltitudeMode = 1;
        public int hoverSpeed;
        public Item[] items;
        public double[] plannedHomePosition;
        public final int vehicleType = 2;
        public final int version = 2;

        public Mission(
                int cruiseSpeed, int hoverSpeed, Item[] items, double[] plannedHomePosition) {
            this.cruiseSpeed = cruiseSpeed;
            this.hoverSpeed = hoverSpeed;
            this.items = items;
            this.plannedHomePosition = plannedHomePosition;
        }
    }

    /**
     * Represents a discrete command or navigational waypoint interpreted by the flight controller.
     */
    public static class Item {
        public Double AMSLAltAboveTerrain = null;
        public int Altitude;
        public int AltitudeMode;
        public boolean autoContinue = true;
        public int command;
        public Integer doJumpId = null;
        public int frame;
        public Double[] params;
        public final String type = "SimpleItem";

        public Item(
                Double AMSLAltAboveTerrain,
                int altitude,
                int altitudeMode,
                boolean autoContinue,
                int command,
                Integer doJumpId,
                int frame,
                Double[] params) {
            this.AMSLAltAboveTerrain = AMSLAltAboveTerrain;
            this.Altitude = altitude;
            this.AltitudeMode = altitudeMode;
            this.autoContinue = autoContinue;
            this.command = command;
            this.doJumpId = doJumpId;
            this.frame = frame;
            this.params = params;
        }
    }
}
