package com.souma1024.shogiv2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class GameController {
    
    @PostMapping("/games/{roomId}")
    public String gameStart() {
        return "game";
    }

    @GetMapping("/games/{roomId}")
    public String game() {
        return "game";
    }
}
