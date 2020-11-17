package com.example.zaitian.CSE416server.model;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Table(name="job")
@Getter
@Setter
public class Job {

    @Id
    private long id;

    @Column(name="state")
    private String state;

    @Column(name ="number_of_districtings")
    private int numberOfDistrictings;

    @Column(name= "compactness_limit")
    private double compactnessLimit;

    @Column(name="population_diff_Limit")
    private double populationDiffLimit;

    @Column(name="status")
    private String status; // "running","aborted","finished"

    @Column(name="result_location")
    private String resultLocation;

    public Job() {
    }

    @Override
    public String toString(){
        return "Job id "+this.getId();
    }

    public void cancelRunning(){
        this.setStatus("aborted");
        //TODO: call the server or seawulf to cancel
    }
    public void startRunning(){
        this.setStatus("running");
        //TODO: call the server or seawulf to start the job
        //hint: using process builder
    }
}
