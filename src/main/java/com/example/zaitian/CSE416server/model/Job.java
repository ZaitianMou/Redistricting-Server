package com.example.zaitian.CSE416server.model;
import javax.persistence.*;

@Entity
@Table(name="job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="state")
    private String state;

    @Column(name ="numberOfDistrictings")
    private int numberOfDistrictings;

    @Column(name= "compactnessLimit")
    private double compactnessLimit;

    @Column(name="populationDiffLimit")
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
    @Override
    public String toString(){
        return "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
