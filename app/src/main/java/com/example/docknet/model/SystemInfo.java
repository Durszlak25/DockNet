package com.example.docknet.model;

import java.util.Map;

public class SystemInfo {
    public String name;
    public String primaryStarType;
    public String primaryStarName;
    public boolean isScoopable;
    public double x, y, z;
    public boolean coordsLocked;
    public Map<String, String> information;
    public long population;

    public SystemInfo() {}
}
