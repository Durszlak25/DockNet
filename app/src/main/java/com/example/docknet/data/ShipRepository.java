package com.example.docknet.data;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.docknet.model.Ship;
import com.example.docknet.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShipRepository {
    // fallback static list
    public static List<Ship> getShips() {
        List<Ship> list = new ArrayList<>();
        list.add(new Ship("Adder", "Small, fast multipurpose ship.", R.drawable.adder_1));
        list.add(new Ship("Anaconda", "Large, powerful and flexible vessel.", R.drawable.anaconda_1));
        list.add(new Ship("Cobra Mk IV", "Well-rounded combat ship.", R.drawable.cobra_mk_4_1));
        list.add(new Ship("Python", "Good cargo and combat balance.", R.drawable.python_1));
        list.add(new Ship("Sidewinder", "Starter ship, nimble and cheap.", R.drawable.sidewinder_mk1));
        return list;
    }

    public static List<Ship> getShips(Context ctx) {
        List<Ship> list = new ArrayList<>();
        if (ctx == null) return getShips();
        try {
            AssetManager am = ctx.getAssets();
            InputStream is = am.open("ships.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject bucket = arr.getJSONObject(i);
                // each bucket has a single key: category name
                java.util.Iterator<String> keys = bucket.keys();
                if (!keys.hasNext()) continue;
                String category = keys.next();
                JSONArray shipsArr = bucket.getJSONArray(category);
                for (int j = 0; j < shipsArr.length(); j++) {
                    JSONObject o = shipsArr.getJSONObject(j);
                    String name = o.optString("name", "");
                    String desc = o.optString("description", "");
                    String file = o.optString("file", null);
                    Integer resId = null;
                    if (file != null && !file.isEmpty()) {
                        // normalize file name to resource name (lowercase, replace non-alnum with underscore, strip extension)
                        String base = file.replaceAll("\\.[^.]*$", "");
                        String resName = base.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
                        resId = ctx.getResources().getIdentifier(resName, "drawable", ctx.getPackageName());
                        if (resId == 0) resId = null;
                    }
                    list.add(new Ship(name, desc, resId, category));
                }
            }
        } catch (Exception e) {
            // fallback to static
            return getShips();
        }
        return list;
    }
}
