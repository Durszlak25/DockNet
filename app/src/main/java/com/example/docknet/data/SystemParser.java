package com.example.docknet.data;

import com.example.docknet.model.SystemInfo;

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
            map.put("allegiance", inf.optString("allegiance", ""));
            map.put("government", inf.optString("government", ""));
            map.put("faction", inf.optString("faction", ""));
            map.put("factionState", inf.optString("factionState", ""));
            map.put("security", inf.optString("security", ""));
            map.put("economy", inf.optString("economy", ""));
            map.put("secondEconomy", inf.optString("secondEconomy", ""));
            map.put("reserve", inf.optString("reserve", ""));
            info.information = map;
        }
        return info;
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
            if (info.population >= 0) sb.append("Population: ").append(String.format(Locale.US, "%,d", info.population)).append('\n');
            appendDetail(sb, "Security", info.information.get("security"));
            String economy = info.information.get("economy");
            String secondEconomy = info.information.get("secondEconomy");
            if (economy != null && !economy.isEmpty()) {
                sb.append("Economy: ").append(economy);
                if (secondEconomy != null && !secondEconomy.isEmpty()) sb.append(" / ").append(secondEconomy);
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
}
