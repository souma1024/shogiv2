package com.souma1024.shogiv2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class GameStartController {
    
    @PostMapping("/games")
    public String gameStart() {
        return "game";
    }

}
