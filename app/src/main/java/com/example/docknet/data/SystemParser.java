package com.example.docknet.data;

import com.example.docknet.model.SystemInfo;
import com.example.docknet.model.SystemSummary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SystemParser {
    public static List<String> parseSystemListFromJson(String jsonResponse) throws JSONException {
        List<String> systemNames = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonResponse);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject systemObject = jsonArray.getJSONObject(i);
            systemNames.add(systemObject.optString("name", ""));
        }
        return systemNames;
    }

    public static SystemInfo parseSystemInfoFromJson(JSONObject obj) {
        SystemInfo info = new SystemInfo();
        info.name = obj.optString("name", "Unknown");

        JSONObject pso = obj.optJSONObject("primaryStar");
        if (pso != null) {
            info.primaryStarType = pso.optString("type", "");
            info.primaryStarName = pso.optString("name", "");
            info.isScoopable = pso.optBoolean("isScoopable", false);
        }

        JSONObject coords = obj.optJSONObject("coords");
        if (coords != null) {
            info.x = coords.optDouble("x", 0);
            info.y = coords.optDouble("y", 0);
            info.z = coords.optDouble("z", 0);
        }

        info.coordsLocked = obj.optBoolean("coordsLocked", false);
        info.population = obj.optLong("population", -1);

        JSONObject inf = obj.optJSONObject("information");
        if (inf != null) {
            Map<String, String> map = new HashMap<>();
            putIfNotEmpty(map, "allegiance", inf.optString("allegiance", ""));
            putIfNotEmpty(map, "government", inf.optString("government", ""));
            putIfNotEmpty(map, "faction", inf.optString("faction", ""));
            putIfNotEmpty(map, "factionState", inf.optString("factionState", ""));
            putIfNotEmpty(map, "security", inf.optString("security", ""));
            putIfNotEmpty(map, "economy", inf.optString("economy", ""));
            putIfNotEmpty(map, "secondEconomy", inf.optString("secondEconomy", ""));
            putIfNotEmpty(map, "reserve", inf.optString("reserve", ""));
            info.information = map;
        }
        return info;
    }

    private static void putIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) map.put(key, value);
    }

    public static String formatSystemInfo(SystemInfo info) {
        if (info == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(info.name);
        if (info.primaryStarType != null && !info.primaryStarType.isEmpty()) {
            sb.append(" — ").append(info.primaryStarType);
        }
        if (info.isScoopable) sb.append(" (scoopable)");
        sb.append('\n');

        if (info.primaryStarName != null && !info.primaryStarName.isEmpty() && !info.primaryStarName.equals(info.name)) {
            sb.append("Star: ").append(info.primaryStarName).append('\n');
        }

        sb.append(String.format(Locale.US, "Coords: [%.2f, %.2f, %.2f]%s\n", info.x, info.y, info.z, info.coordsLocked ? " (locked)" : ""));

        if (info.information != null) {
            appendDetail(sb, "Allegiance", info.information.get("allegiance"));
            appendDetail(sb, "Government", info.information.get("government"));
            appendDetail(sb, "Faction", info.information.get("faction"));
            appendDetail(sb, "Faction State", info.information.get("factionState"));
            if (info.population >= 0)
                sb.append("Population: ").append(String.format(Locale.US, "%,d", info.population)).append('\n');
            appendDetail(sb, "Security", info.information.get("security"));
            String economy = info.information.get("economy");
            String secondEconomy = info.information.get("secondEconomy");
            if (economy != null && !economy.isEmpty()) {
                sb.append("Economy: ").append(economy);
                if (secondEconomy != null && !secondEconomy.isEmpty())
                    sb.append(" / ").append(secondEconomy);
                sb.append('\n');
            }
            appendDetail(sb, "Reserve", info.information.get("reserve"));
        }
        return sb.toString().trim();
    }

    private static void appendDetail(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append('\n');
        }
    }

    public static Map<String, String> infoFormatter(SystemInfo info) {
        Map<String, String> systemMap = new HashMap<>();

        systemMap.put("systemName", info.name != null ? info.name : "");
        systemMap.put("starName", info.primaryStarName != null ? info.primaryStarName : "");
        systemMap.put("starType", info.primaryStarType != null ? info.primaryStarType : "");
        systemMap.put("isScoopable", info.isScoopable ? "Scoopable" : "");
        systemMap.put("coordinates_x", String.valueOf(info.x));
        systemMap.put("coordinates_y", String.valueOf(info.y));
        systemMap.put("coordinates_z", String.valueOf(info.z));
        systemMap.put("coordinatesLocked", info.coordsLocked ? "locked" : "");
        systemMap.put("population", String.valueOf(info.population));

        String distanceToSol = String.valueOf(calculateDistanceToOrigin(info.x, info.y, info.z));
        systemMap.put("distanceToSol", distanceToSol);

        systemMap.put("allegiance", info.information != null ? info.information.getOrDefault("allegiance", "") : "");

        return systemMap;
    }

    public static SystemSummary toSummary(SystemInfo info) {
        if (info == null) return null;
        double distance = calculateDistanceToOrigin(info.x, info.y, info.z);
        String allegiance = info.information != null ? info.information.getOrDefault("allegiance", "") : "";
        return new SystemSummary(
                info.name,
                info.primaryStarName != null ? info.primaryStarName : "",
                info.primaryStarType != null ? info.primaryStarType : "",
                info.isScoopable,
                info.x, info.y, info.z,
                info.coordsLocked,
                info.population,
                distance,
                allegiance
        );
    }

    private static double calculateDistanceToOrigin(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

}
