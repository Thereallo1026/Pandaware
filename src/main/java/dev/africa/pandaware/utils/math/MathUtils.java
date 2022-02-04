package dev.africa.pandaware.utils.math;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class MathUtils {
    public double getAverageDouble(List<Double> list) {
        if (list.size() <= 0) {
            return 0;
        } else {
            double average = 0;

            for (double value : list) {
                average += value;
            }

            return average / list.size();
        }
    }

    public double roundToDecimal(double number, double places) {
        return Math.round(number * Math.pow(10, places)) / Math.pow(10, places);
    }

    public double roundToIncrement(double value, double increment) {
        return increment * (Math.round(value / increment));
    }
}
