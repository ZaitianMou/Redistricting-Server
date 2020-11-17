package com.example.zaitian.CSE416server.handler;
import com.example.zaitian.CSE416server.accessingdatajpa.JobRepository;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

//@Service component???
public class JobHandler {

//    @Autowired
     private JobRepository jobRepository;

    public void createJob(Job newJob){

        System.out.println("Got You!");
        System.out.println("Number of districtings: "+newJob.getNumberOfDistrictings());
        System.out.println("State: "+newJob.getState());
        System.out.println("Population difference: "+newJob.getPopulationDiffLimit());
    }

    public Job getJob(long id){
        return jobRepository.findById(id).get();
    }

    public List<Job> getJobs(){
        List<Job> jobs= (List<Job>) jobRepository.findAll();

        return jobs;
    }

    public void deleteJob(long id){
        System.out.println("Delete Job: "+id);
    }
    public void cancelJob(long id){
        System.out.println("Cancel job: "+id);
    }

}
