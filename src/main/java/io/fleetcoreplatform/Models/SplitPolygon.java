package io.fleetcoreplatform.Models;

import org.postgis.Polygon;

public record SplitPolygon(Polygon left, Polygon right) {}
