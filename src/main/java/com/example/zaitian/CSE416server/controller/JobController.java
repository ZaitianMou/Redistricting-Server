package com.example.zaitian.CSE416server.controller;
import com.example.zaitian.CSE416server.accessingdatajpa.JobRepository;
import com.example.zaitian.CSE416server.handler.JobHandler;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    JobHandler jobHandler=new JobHandler();

    @PostMapping("/job")
    public void creatJob(@RequestBody Job newJob){
        System.out.println(newJob.getNumberOfDistrictings());
        this.jobHandler.createJob(newJob);
    }

    @DeleteMapping("/job/{id}")
    public void deleteJob(@PathVariable Integer id){
        jobHandler.deleteJob(id);
    }

    @PatchMapping("/job/{id}/cancel")
    public void cancelJob(@PathVariable Integer id){
        jobHandler.cancelJob(id);
    }

    @GetMapping("/job/{id}")
    public Job getJob(@PathVariable Long id){
        Optional<Job> job=jobRepository.findById(id);
        return job.get();
    }
}
