package com.example.docknet.util;

@SuppressWarnings("unused")
public final class DistanceUtils {
    private DistanceUtils() { }

    public static double calculateDistance(double x1, double y1, double z1,
                                           double x2, double y2, double z2) {
        if (x1 == x2 && y1 == y2 && z1 == z2) return 0.0;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
