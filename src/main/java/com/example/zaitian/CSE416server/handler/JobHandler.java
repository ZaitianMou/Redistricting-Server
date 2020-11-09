package com.example.zaitian.CSE416server.handler;

import com.example.zaitian.CSE416server.model.Job;
import org.springframework.web.bind.annotation.PathVariable;

public class JobHandler {


    public void createJob(Job newJob){

        System.out.println("Got You!");
        System.out.println("Number of districtings: "+newJob.getNumberOfDistrictings());
        System.out.println("State: "+newJob.getState());
        System.out.println("Population difference: "+newJob.getPopulationDiffLimit());
    }

    public void deleteJob(int id){

        System.out.println("Delete Job: "+id);
    }
    public void cancelJob(int id){
        System.out.println("Cancel job: "+id);
    }
}
