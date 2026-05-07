package com.example.ExpenseTracker.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/admin")
    public String testAdmin(){
        return "Only admin here";
    }


}

