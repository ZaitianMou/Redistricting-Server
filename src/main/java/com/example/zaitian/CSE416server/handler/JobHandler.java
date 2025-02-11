package com.example.zaitian.CSE416server.handler;
import com.example.zaitian.CSE416server.repository.JobRepository;
import com.example.zaitian.CSE416server.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
@Service
public class JobHandler {
    @Autowired
    private JobRepository jobRepository;

    public void createJob(Job newJob) throws IOException {
        newJob.startRunning();
        jobRepository.save(newJob);
    }
    public Job getJob(long id){
        return jobRepository.findById(id).get();
    }

    public List<Job> getJobs(){
        return jobRepository.findAll();
    }

    public void deleteJob(long id){
        jobRepository.deleteById(id);
    }

    public Job cancelJob(long id) throws IOException {
        Job job = getJob(id);
        job.cancelRunning();
        jobRepository.deleteById(id);
        return job;
    }

    public String getJobResult(long id) throws IOException, InterruptedException {
        Job job = getJob(id);
        return job.getResult(id);
    }
//    public String getBoxplot(long id) throws FileNotFoundException {
//        Job job = getJob(id);
//        return job.getBoxplot(id);
//    }

}
