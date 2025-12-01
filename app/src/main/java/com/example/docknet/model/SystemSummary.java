package com.example.docknet.model;

public class SystemSummary {
    public final String systemName;
    public final String starName;
    public final String starType;
    public final boolean isScoopable;
    public final double x;
    public final double y;
    public final double z;
    public final boolean coordsLocked;
    public final long population;
    public final double distanceToSol;
    public final String allegiance;

    public SystemSummary(String systemName, String starName, String starType, boolean isScoopable,
                         double x, double y, double z, boolean coordsLocked, long population,
                         double distanceToSol, String allegiance) {
        this.systemName = systemName;
        this.starName = starName;
        this.starType = starType;
        this.isScoopable = isScoopable;
        this.x = x;
        this.y = y;
        this.z = z;
        this.coordsLocked = coordsLocked;
        this.population = population;
        this.distanceToSol = distanceToSol;
        this.allegiance = allegiance;
    }
}
