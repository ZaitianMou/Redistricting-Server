package com.example.zaitian.CSE416server.controller;
import com.example.zaitian.CSE416server.accessingdatajpa.JobRepository;
import com.example.zaitian.CSE416server.handler.JobHandler;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController()
public class JobController {

    @Autowired
    private JobRepository jobRepository;
    JobHandler jobHandler=new JobHandler();

    @PostMapping("/job")
    public ResponseEntity creatJob(@RequestBody Job newJob){
        try {
            newJob.startRunning();
            jobRepository.save(newJob);
            return new ResponseEntity( HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/job/{id}")
    public ResponseEntity getJob(@PathVariable Long id){
        Job job=jobRepository.findById(id).get();
        ResponseEntity response=new ResponseEntity(job,HttpStatus.OK);
        return response;
    }

    @GetMapping("/jobs")
    public ResponseEntity getJobs(){
        List<Job> jobs= jobRepository.findAll();
        ResponseEntity response=new ResponseEntity(jobs,HttpStatus.OK);
        return response;
    }

    @DeleteMapping("/job/{id}")
    public ResponseEntity deleteJob(@PathVariable Long id){
        try {
            jobRepository.deleteById(id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/job/{id}/cancel")
    public ResponseEntity cancelJob(@PathVariable Long id){
        Optional<Job> job = jobRepository.findById(id);
        if (job.isPresent()){
            job.get().cancelRunning();
            jobRepository.save(job.get());
            return new ResponseEntity(job.get(),HttpStatus.OK);
        }
        else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}
