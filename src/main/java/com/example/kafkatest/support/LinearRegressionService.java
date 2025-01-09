package com.example.kafkatest.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
public class LinearRegressionService {
    private List<Double> x;
    private List<Double> y;

    public LinearRegressionService(List<Double> x, List<Double> y) {
        if(x.size() != y.size())
            throw new IllegalArgumentException("Illegal Arguments caused by not same elements in list of x and y");
        this.x = x;
        this.y = y;
    }

    public double getRSquared() {
        final int n = x.size();
        final double A = n * squaredSumOfX() - sumOfX() * sumOfX();
        final double B = n * squaredSumOfY() - sumOfY() * sumOfY();
        final double C = n * sumOfXY() - sumOfX() * sumOfY();

        final double sqrtAB = Math.sqrt(A * B);
        final double R = C / sqrtAB;

        log.info("here is R Value : {}", R);

        return R * R;
    }

    private double sumOfX() {
        return x.stream().reduce(0.0, Double::sum);
    }

    private double sumOfY() {
        return y.stream().reduce(0.0, Double::sum);
    }

    private double sumOfXY() {
        double sum = 0.0;
        for(int i=0; i<x.size(); i++) {
            sum += (x.get(i) * y.get(i));
        }

        return sum;
    }

    private double squaredSumOfX() {
        return x.stream().reduce(0.0, (subtotal, value) -> subtotal + value * value);
    }

    private double squaredSumOfY() {
        return y.stream().reduce(0.0, (subtotal, value) -> subtotal + value * value);
    }
}
