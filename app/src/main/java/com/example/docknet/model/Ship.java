package com.example.docknet.model;

public class Ship {
    public final String name;
    public final String description;
    public final Integer resId;
    public final String category;

    public Ship(String name, String description, Integer resId) {
        this(name, description, resId, null);
    }

    public Ship(String name, String description, Integer resId, String category) {
        this.name = name;
        this.description = description;
        this.resId = resId;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ship)) return false;
        Ship s = (Ship) o;
        boolean sameRes = (resId == null && s.resId == null) || (resId != null && resId.equals(s.resId));
        boolean sameCat = (category == null && s.category == null) || (category != null && category.equals(s.category));
        return name.equals(s.name) && sameRes && sameCat;
    }
}
