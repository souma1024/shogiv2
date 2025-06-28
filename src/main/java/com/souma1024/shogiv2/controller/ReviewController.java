package com.souma1024.shogiv2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class ReviewController {

    @GetMapping("/review")
    public String review() {
        return "review";
    }
}
