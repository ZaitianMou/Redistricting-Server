package com.example.zaitian.CSE416server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Boxplot {
    private List<Double> mins;
    private List<Double> q1s;
    private List<Double> medians;
    private List<Double> q3s;
    private List<Double> maxs;

    @Override
    public String toString() {
        return "Boxplot{" +
                "mins=" + mins +
                ", q1s=" + q1s +
                ", medians=" + medians +
                ", q3s=" + q3s +
                ", maxs=" + maxs +
                '}';
    }
}
