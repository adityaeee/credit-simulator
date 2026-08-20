package com.aditya.creditsimulator.vehicle;

public final class Vehicle {

    private final VehicleType type;
    private final VehicleCondition condition;
    private final int year;

    public Vehicle(VehicleType type, VehicleCondition condition, int year) {
        this.type = type;
        this.condition = condition;
        this.year = year;
    }

    public VehicleType getType() {
        return type;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return type.name() + " " + condition.name() + " (" + year + ")";
    }
}