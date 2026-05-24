package com.pharmacy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/pos")
    public String pos() {
        return "pos/index";
    }

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory/index";
    }
}
