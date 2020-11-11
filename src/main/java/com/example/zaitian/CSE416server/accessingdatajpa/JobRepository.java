package com.example.zaitian.CSE416server.accessingdatajpa;

import com.example.zaitian.CSE416server.model.Job;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Component
public interface JobRepository extends CrudRepository<Job, Integer> {
    Optional<Job> findById(Integer id);
}
