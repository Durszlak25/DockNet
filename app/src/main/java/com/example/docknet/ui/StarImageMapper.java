package com.example.docknet.ui;

import com.example.docknet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarImageMapper {
    private static final Map<String, Integer> STAR_MAP = new HashMap<>();

    static {
        STAR_MAP.put("o (blue-white) star", R.drawable.star_1);
        STAR_MAP.put("b (blue-white) star", R.drawable.star_2);
        STAR_MAP.put("b (blue-white super giant) star", R.drawable.star_2);
        STAR_MAP.put("a (blue-white) star", R.drawable.star_3);
        STAR_MAP.put("a (blue-white super giant) star", R.drawable.star_3);
        STAR_MAP.put("f (white) star", R.drawable.star_4);
        STAR_MAP.put("f (white super giant) star", R.drawable.star_4);
        STAR_MAP.put("g (white-yellow) star", R.drawable.star_5);
        STAR_MAP.put("g (white-yellow super giant) star", R.drawable.star_5);
        STAR_MAP.put("k (yellow-orange) star", R.drawable.star_6);
        STAR_MAP.put("k (yellow-orange giant) star", R.drawable.star_6);
        STAR_MAP.put("m (red dwarf) star", R.drawable.star_7);
        STAR_MAP.put("m (red giant) star", R.drawable.star_7);
        STAR_MAP.put("m (red super giant) star", R.drawable.star_7);
        STAR_MAP.put("l (brown dwarf) star", R.drawable.star_8);
        STAR_MAP.put("t (brown dwarf) star", R.drawable.star_9);
        STAR_MAP.put("y (brown dwarf) star", R.drawable.star_10);
        STAR_MAP.put("t tauri star", R.drawable.star_11);
        STAR_MAP.put("herbig ae/be star", R.drawable.star_12);
        STAR_MAP.put("wolf-rayet n star", R.drawable.star_22);
        STAR_MAP.put("wolf-rayet nc star", R.drawable.star_23);
        STAR_MAP.put("wolf-rayet c star", R.drawable.star_24);
        STAR_MAP.put("wolf-rayet o star", R.drawable.star_25);
        STAR_MAP.put("c star", R.drawable.star_32);
        STAR_MAP.put("cn star", R.drawable.star_33);
        STAR_MAP.put("cj star", R.drawable.star_42);
        STAR_MAP.put("ms-type star", R.drawable.star_42);
        STAR_MAP.put("s-type star", R.drawable.star_42);
        STAR_MAP.put("white dwarf (d) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (da) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dab) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (daz) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dav) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (db) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dbz) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dbv) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (do) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dov) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dq) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dc) star", R.drawable.star_51);
        STAR_MAP.put("white dwarf (dcv) star", R.drawable.star_51);
        STAR_MAP.put("neutron star", R.drawable.star_91);
        STAR_MAP.put("black hole", R.drawable.star_92);
        STAR_MAP.put("supermassive black hole", R.drawable.star_92);
    }

    public static Integer getResId(String starType) {
        if (starType == null || starType.isEmpty()) return null;
        return STAR_MAP.get(starType.toLowerCase());
    }

    public static List<Map.Entry<String, Integer>> getStarEntries() {
        return new ArrayList<>(STAR_MAP.entrySet());
    }
}

