package com.souma1024.shogiv2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class IndexController {
    
    @GetMapping("/")
    public String Hello() {
        return "index";
    }

    @GetMapping("/test")
    public String test() {
        return "websocket-test.html";
    }
}
