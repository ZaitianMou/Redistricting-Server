package com.example.zaitian.CSE416server.controller;

import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api")
public class GeoController {



    @GetMapping("/demo/{state}")
    String creatJob(@PathVariable String state) throws IOException {

//        File resource=new ClassPathResource("data/AR/AR_boundary.geo.json").getFile();
        File resource=ResourceUtils.getFile("classpath:data/AR/AR_boundary.geo.json");

        return new String(Files.readAllBytes((resource.toPath())));

    }
}
