package com.example.testalbum;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/test/{name}")
    public String myName(@PathVariable String name) {
        return "Hello " + name;
    }
}
