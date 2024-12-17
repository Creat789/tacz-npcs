package com.corrinedev.tacznpcs.common.entity;

import com.google.gson.annotations.SerializedName;

public enum Rank {
    @SerializedName("Rookie")
    ROOKIE("Rookie"),
    @SerializedName("Experienced")
    EXPERIENCED("Experienced"),
    @SerializedName("Veteran")
    VETERAN("Veteran"),
    @SerializedName("Expert")
    EXPERT("Expert");
    public String rankname;
    Rank(String name) {
        rankname = name;
    }

    @Override
    public String toString() {
        return rankname;
    }
}
