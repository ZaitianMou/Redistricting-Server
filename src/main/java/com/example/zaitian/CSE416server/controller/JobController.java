package com.example.zaitian.CSE416server.controller;
import com.example.zaitian.CSE416server.handler.JobHandler;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController()
public class JobController {

    @Autowired
    JobHandler jobHandler=new JobHandler();

    @PostMapping("/job")
    public ResponseEntity createJob(@RequestBody Job newJob) throws IOException {
       // try {
            System.out.println("Creating a job!");
            jobHandler.createJob(newJob);
            return new ResponseEntity( HttpStatus.CREATED);
//        } catch (Exception e) {
//            return new ResponseEntity( HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    @GetMapping("/job/{id}")
    public ResponseEntity getJob(@PathVariable Long id){
        try {
            Job job = jobHandler.getJob(id);
            return new ResponseEntity(job, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity getJobs(){
        try {
            List<Job> jobs = jobHandler.getJobs();
            return new ResponseEntity(jobs, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/job/{id}")
    public ResponseEntity deleteJob(@PathVariable Long id){
        try {
            jobHandler.deleteJob(id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/job/{id}/cancel")
    public ResponseEntity cancelJob(@PathVariable Long id){
        try {
            System.out.println("Cancel job: "+id);
            Job job = jobHandler.cancelJob(id);
            return new ResponseEntity(job, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println("123");
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
