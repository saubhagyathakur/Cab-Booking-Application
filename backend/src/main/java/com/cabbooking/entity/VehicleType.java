package com.cabbooking.entity;

public enum VehicleType {
    ECONOMY(1.0, 3.0, 1.2),   // multiplier, baseFare, perKmRate
    COMFORT(1.3, 5.0, 1.6),
    PREMIER(1.8, 8.0, 2.2),
    XL(2.2, 10.0, 2.8);

    private final double multiplier;
    private final double baseFare;
    private final double perKmRate;

    VehicleType(double multiplier, double baseFare, double perKmRate) {
        this.multiplier = multiplier;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPerKmRate() {
        return perKmRate;
    }
}
