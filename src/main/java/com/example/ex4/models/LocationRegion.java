package com.example.ex4.models;

public enum LocationRegion {
    NORTH("צפון"),
    VALLEY("בקעה"),
    CENTER("מרכז"),
    JERUSALEM_AND_SURROUNDINGS("ירושלים והסביבה"),
    JUDEA_AND_SAMARIA("יהודה ושומרון"),
    GAZA("עזה"),
    SOUTH("דרום");

    private final String hebrewName;

    LocationRegion(String hebrewName) {
        this.hebrewName = hebrewName;
    }

    public String getHebrewName() {
        return hebrewName;
    }

    @Override
    public String toString() {
        return hebrewName;
    }
} 