package com.corrinedev.tacznpcs.common.entity;

public enum Rank {
    ROOKIE("Rookie"),
    EXPERIENCED("Experienced"),
    VETERAN("Veteran"),
    EXPERT("Expert");
    public final String rankname;
    Rank(String name) {
        rankname = name;
    }

    @Override
    public String toString() {
        return rankname;
    }
}
