package io.fleetcoreplatform.Models;

/**
 * A lightweight carrier for parallel coordinate arrays. Extracts vertex structures into flat arrays
 * for cache-friendly matrix and vector calculations.
 *
 * @param xs Sequence of longitudinal or X-axis values.
 * @param ys Sequence of latitudinal or Y-axis values.
 */
public record XYArrays(double[] xs, double[] ys) {}
