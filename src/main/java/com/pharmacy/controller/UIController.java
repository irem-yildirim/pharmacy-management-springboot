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

    @GetMapping("/purchase")
    public String purchase() {
        return "purchase/index";
    }

    @GetMapping("/customer")
    public String customer() {
        return "customer/index";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings/index";
    }

    @GetMapping("/account")
    public String account() {
        return "account/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login/index";
    }
}
