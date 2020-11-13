package com.example.zaitian.CSE416server.accessingdatajpa;

import com.example.zaitian.CSE416server.model.Job;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends CrudRepository<Job, Long> {
    List<Job> findByState(String state);

    Optional<Job> findById(Long id);
}
