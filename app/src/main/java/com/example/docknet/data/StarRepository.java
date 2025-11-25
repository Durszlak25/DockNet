package com.example.docknet.data;

import com.example.docknet.model.Star;
import com.example.docknet.ui.StarImageMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StarRepository {
    public static List<Star> getStars() {
        List<Map.Entry<String, Integer>> entries = StarImageMapper.getStarEntries();
        List<Star> stars = new ArrayList<>();
        for (Map.Entry<String, Integer> e : entries) {
            stars.add(new Star(e.getKey(), e.getValue()));
        }
        return stars;
    }
}

