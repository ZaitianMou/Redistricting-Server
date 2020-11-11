package com.example.zaitian.CSE416server.controller;

import com.example.zaitian.CSE416server.accessingdatajpa.JobRepository;
import com.example.zaitian.CSE416server.handler.JobHandler;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@Component
public class JobController {
    JobHandler jobHandler=new JobHandler();

//    public JobController(JobRepository jobRepository) {
//        this.jobRepository = jobRepository;
//    }

    @Autowired
    private JobRepository jobRepository;

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
    public Job getJob(@PathVariable Integer id){
        System.out.println("!!!!!"+id);
        Iterable<Job> jobs=jobRepository.findAll();
        //System.out.println(job.get().getState());
        System.out.println("!!");
        return null;
    }
}
