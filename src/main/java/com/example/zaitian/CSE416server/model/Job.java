package com.example.zaitian.CSE416server.model;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Job")
public class Job {

    @Id
    private int id;
    private String state;
    private int numberOfDistrictings;
    private double compactnessLimit;
    private double populationDiffLimit;

    public Job(String id, String state, String numberOfDistrictings, String compactnessLimit, String populationDiffLimit) {
        System.out.println("Job Constructor!");
        this.id = Integer.getInteger(id);
        this.state = state;
        this.numberOfDistrictings = Integer.getInteger(numberOfDistrictings);
        this.compactnessLimit = Double.valueOf(compactnessLimit);
        this.populationDiffLimit = Double.valueOf(populationDiffLimit);
    }

    public Job() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getNumberOfDistrictings() {
        return numberOfDistrictings;
    }

    public void setNumberOfDistrictings(int numberOfDistrictings) {
        this.numberOfDistrictings = numberOfDistrictings;
    }

    public double getCompactnessLimit() {
        return compactnessLimit;
    }

    public void setCompactnessLimit(double compactnessLimit) {
        this.compactnessLimit = compactnessLimit;
    }

    public double getPopulationDiffLimit() {
        return populationDiffLimit;
    }

    public void setPopulationDiffLimit(double populationDiffLimit) {
        this.populationDiffLimit = populationDiffLimit;
    }

}
