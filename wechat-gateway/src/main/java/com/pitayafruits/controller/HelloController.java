package com.pitayafruits.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/g")
public class HelloController {

    @GetMapping("/hello")
    public Object hello() {
        return "Hello World";
    }

}
