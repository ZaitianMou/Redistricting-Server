package com.example.zaitian.CSE416server.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/get_state_demo={state}")
    void getStateDemo(@PathVariable String state){

        System.out.println("Get state demo: "+state);

    }
}
