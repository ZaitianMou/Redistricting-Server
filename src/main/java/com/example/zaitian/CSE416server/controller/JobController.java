package com.example.zaitian.CSE416server.controller;

import com.example.zaitian.CSE416server.handler.JobHandler;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class JobController {

    JobHandler jobHandler=new JobHandler();

    @PostMapping("/job")
    public void creatJob(@RequestBody Job newJob){
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

}
