package com.example.docknet.model;

public class Star {
    public final String name;
    public final Integer resId;

    public Star(String name, Integer resId) {
        this.name = name;
        this.resId = resId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Star star = (Star) o;
        return name.equals(star.name) && ((resId == null && star.resId == null) || (resId != null && resId.equals(star.resId)));
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (resId != null ? resId.hashCode() : 0);
        return result;
    }
}

