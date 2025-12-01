package com.example.docknet.data;

import android.annotation.SuppressLint;
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
    public static List<Ship> getShips() {
        List<Ship> list = new ArrayList<>();
        list.add(new Ship("Adder", "Small, fast multipurpose ship.", R.drawable.adder_1));
        list.add(new Ship("Anaconda", "Large, powerful and flexible vessel.", R.drawable.anaconda_1));
        list.add(new Ship("Cobra Mk IV", "Well-rounded combat ship.", R.drawable.cobra_mk_4_1));
        list.add(new Ship("Python", "Good cargo and combat balance.", R.drawable.python_1));
        list.add(new Ship("Sidewinder", "Starter ship, nimble and cheap.", R.drawable.sidewinder_mk1));
        return list;
    }

    @SuppressLint("DiscouragedApi")
    public static List<Ship> getShips(Context ctx) {
        final List<Ship> list = new ArrayList<>();
        if (ctx == null) return getShips();
        try {
            final AssetManager am = ctx.getAssets();
            try (InputStream is = am.open("ships.json");
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                final JSONArray arr = new JSONArray(sb.toString());
                for (int i = 0; i < arr.length(); i++) {
                    final JSONObject bucket = arr.getJSONObject(i);
                    java.util.Iterator<String> keys = bucket.keys();
                    if (!keys.hasNext()) continue;
                    final String category = keys.next();
                    final JSONArray shipsArr = bucket.getJSONArray(category);
                    for (int j = 0; j < shipsArr.length(); j++) {
                        final JSONObject o = shipsArr.getJSONObject(j);
                        final String name = o.optString("name", "");
                        final String desc = o.optString("description", "");
                        final String file = o.optString("file", "");
                        Integer resId = null;
                        if (!file.isEmpty()) {
                            final String base = file.replaceAll("\\.[^.]*$", "");
                            final String resName = base.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
                            int id = ctx.getResources().getIdentifier(resName, "drawable", ctx.getPackageName());
                            if (id != 0) resId = id;
                        }
                        list.add(new Ship(name, desc, resId, category));
                    }
                }
            }
        } catch (Exception e) {
            return getShips();
        }
        return list;
    }
}
