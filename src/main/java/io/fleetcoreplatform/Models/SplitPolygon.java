package io.fleetcoreplatform.Models;

import org.postgis.Polygon;

/**
 * A container holding the results of a binary polygon bisection. Always guarantees two valid
 * geometric regions after a split operation.
 *
 * @param left The first partitioned region resulting from the cut.
 * @param right The second partitioned region resulting from the cut.
 */
public record SplitPolygon(Polygon left, Polygon right) {}
